package tech.illuin.pipeline.observer.descriptor;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Meter.Id;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.initializer.metering.InitializationMarkerManager;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.metering.MeterRegistryKey;
import tech.illuin.pipeline.metering.PipelineMarkerManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.observer.descriptor.describable.DefaultDescribable;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.observer.descriptor.describable.Description;
import tech.illuin.pipeline.observer.descriptor.model.*;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.sink.metering.SinkMarkerManager;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.metering.StepMarkerManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.illuin.pipeline.metering.MeterRegistryKey.*;
import static tech.illuin.pipeline.output.ComponentFamily.*;

public class DescriptorObserver implements Observer
{
    private Supplier<PipelineDescription> supplier;

    @Override
    public <I> void init(
        String id,
        InitializerDescriptor<I> initializer,
        List<StepDescriptor<Indexable, I>> steps,
        List<SinkDescriptor> sinks,
        PipelineErrorHandler errorHandler,
        List<OnCloseHandler> onCloseHandlers,
        MeterRegistry meterRegistry
    ) {
        this.supplier = () -> {
            PipelineTag simulated = new PipelineTag(null, id, null);
            return new PipelineDescription(
                id,
                createInitializer(simulated, initializer, meterRegistry),
                createSteps(simulated, steps, meterRegistry),
                createSinks(simulated, sinks, meterRegistry),
                compileMetrics(
                    meterRegistry,
                    new PipelineMarkerManager(simulated, new MetricTags()).discriminants(),
                    PIPELINE_RUN_KEY,
                    PIPELINE_RUN_TOTAL_KEY,
                    PIPELINE_RUN_SUCCESS_KEY,
                    PIPELINE_RUN_FAILURE_KEY,
                    PIPELINE_RUN_ERROR_TOTAL_KEY
                )
            );
        };
    }

    private static InitializerDescription createInitializer(PipelineTag tag, InitializerDescriptor<?> descriptor, MeterRegistry meterRegistry)
    {
        return new InitializerDescription(
            descriptor.id(),
            compileDescription(descriptor.initializer()),
            compileDescription(descriptor.errorHandler()),
            compileMetrics(
                meterRegistry,
                createMarkerManager(tag, descriptor.id(), INITIALIZER).discriminants(),
                PIPELINE_INITIALIZATION_RUN_KEY,
                PIPELINE_INITIALIZATION_RUN_TOTAL_KEY,
                PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY,
                PIPELINE_INITIALIZATION_RUN_FAILURE_KEY,
                PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY
            )
        );
    }

    private static <I> List<StepDescription> createSteps(PipelineTag tag, List<StepDescriptor<Indexable, I>> descriptors, MeterRegistry meterRegistry)
    {
        return descriptors.stream()
            .map(sd -> new StepDescription(
                sd.id(),
                compileDescription(sd.step()),
                sd.isPinned(),
                compileDescription(sd.executionWrapper()),
                compileDescription(sd.activationPredicate()),
                compileDescription(sd.resultEvaluator()),
                compileDescription(sd.errorHandler()),
                compileMetrics(
                    meterRegistry,
                    createMarkerManager(tag, sd.id(), STEP).discriminants(),
                    PIPELINE_STEP_RUN_KEY,
                    PIPELINE_STEP_RUN_TOTAL_KEY,
                    PIPELINE_STEP_RUN_SUCCESS_KEY,
                    PIPELINE_STEP_RUN_FAILURE_KEY,
                    PIPELINE_STEP_RESULT_TOTAL_KEY,
                    PIPELINE_STEP_ERROR_TOTAL_KEY
                )
            ))
            .toList();
    }

    private static List<SinkDescription> createSinks(PipelineTag tag, List<SinkDescriptor> descriptors, MeterRegistry meterRegistry)
    {
        return descriptors.stream()
            .map(sd -> new SinkDescription(
                sd.id(),
                compileDescription(sd.sink()),
                sd.isAsync(),
                compileDescription(sd.executionWrapper()),
                compileDescription(sd.errorHandler()),
                compileMetrics(
                    meterRegistry,
                    createMarkerManager(tag, sd.id(), SINK).discriminants(),
                    PIPELINE_SINK_RUN_KEY,
                    PIPELINE_SINK_RUN_TOTAL_KEY,
                    PIPELINE_SINK_RUN_SUCCESS_KEY,
                    PIPELINE_SINK_RUN_FAILURE_KEY,
                    PIPELINE_SINK_ERROR_TOTAL_KEY
                )
            ))
            .toList();
    }

    private static MarkerManager createMarkerManager(PipelineTag tag, String componentId, ComponentFamily family)
    {
        return switch (family) {
            case INITIALIZER -> new InitializationMarkerManager(new ComponentTag(null, tag, componentId, family), new MetricTags());
            case STEP -> new StepMarkerManager(new ComponentTag(null, tag, componentId, family), new MetricTags());
            case SINK -> new SinkMarkerManager(new ComponentTag(null, tag, componentId, family), new MetricTags());
        };
    }

    private static Object compileDescription(Object property)
    {
        if (property instanceof Describable describable)
            return compileDescription(describable.describe());
        if (property instanceof Description description)
            return description;
        return new DefaultDescribable(property).describe();
    }

    private static Map<String, Metric> compileMetrics(MeterRegistry meterRegistry, Collection<Tag> discriminants, MeterRegistryKey... keys)
    {
        return Stream.of(keys).collect(Collectors.toMap(
            MeterRegistryKey::id,
            k -> {
                Collection<Meter> meters = meterRegistry.find(k.id()).tags(discriminants).meters();

                Map<Id, Number> values = meters.isEmpty()
                    ? Collections.emptyMap()
                    : meters.stream().collect(Collectors.toMap(
                        Meter::getId,
                        DescriptorObserver::getValue
                    ))
                ;
                return new Metric(k.id(), values);
            }
        ));
    }

    private static Number getValue(Meter meter)
    {
        return switch (meter.getId().getType()) {
            case COUNTER -> ((Counter) meter).count();
            case TIMER -> ((Timer) meter).count();
            case GAUGE -> ((Gauge) meter).value();
            default -> throw new UnsupportedOperationException("Unsupported meter type: " + meter.getId().getType());
        };
    }

    public PipelineDescription describe()
    {
        if (this.supplier == null)
            throw new IllegalStateException("The observer has not been initialized");
        return this.supplier.get();
    }

    @Override
    public void close() {}
}
