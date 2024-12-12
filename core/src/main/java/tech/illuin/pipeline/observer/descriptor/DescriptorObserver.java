package tech.illuin.pipeline.observer.descriptor;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Meter.Id;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.metering.MeterRegistryKey;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.observer.descriptor.describable.DefaultDescribable;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.observer.descriptor.model.*;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.illuin.pipeline.metering.MeterRegistryKey.*;

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
        this.supplier = () -> new PipelineDescription(
            id,
            createInitializer(initializer, meterRegistry),
            createSteps(steps, meterRegistry),
            createSinks(sinks, meterRegistry),
            compileMetrics(
                meterRegistry,
                PIPELINE_RUN_KEY,
                PIPELINE_RUN_TOTAL_KEY,
                PIPELINE_RUN_SUCCESS_KEY,
                PIPELINE_RUN_FAILURE_KEY,
                PIPELINE_RUN_ERROR_TOTAL_KEY
            )
        );
    }

    private static InitializerDescription createInitializer(InitializerDescriptor<?> descriptor, MeterRegistry meterRegistry)
    {
        return new InitializerDescription(
            descriptor.id(),
            compileDescription(descriptor.initializer()),
            compileDescription(descriptor.errorHandler()),
            compileMetrics(
                meterRegistry,
                PIPELINE_INITIALIZATION_RUN_KEY,
                PIPELINE_INITIALIZATION_RUN_TOTAL_KEY,
                PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY,
                PIPELINE_INITIALIZATION_RUN_FAILURE_KEY,
                PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY
            )
        );
    }

    private static <I> List<StepDescription> createSteps(List<StepDescriptor<Indexable, I>> descriptors, MeterRegistry meterRegistry)
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

    private static List<SinkDescription> createSinks(List<SinkDescriptor> descriptors, MeterRegistry meterRegistry)
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
                    PIPELINE_SINK_RUN_KEY,
                    PIPELINE_SINK_RUN_TOTAL_KEY,
                    PIPELINE_SINK_RUN_SUCCESS_KEY,
                    PIPELINE_SINK_RUN_FAILURE_KEY,
                    PIPELINE_SINK_ERROR_TOTAL_KEY
                )
            ))
            .toList();
    }

    private static Object compileDescription(Object property)
    {
        if (property instanceof Describable describable)
            return compileDescription(describable);
        return new DefaultDescribable(property).describe();
    }

    private static Object compileDescription(Describable describable)
    {
        var next = describable.describe();
        if (next instanceof Describable nextDescribable)
            return compileDescription(nextDescribable);
        return next;
    }

    private static Map<String, Metric> compileMetrics(MeterRegistry meterRegistry, MeterRegistryKey... keys)
    {
        return Stream.of(keys).collect(Collectors.toMap(
            MeterRegistryKey::id,
            k -> {
                Collection<Meter> meters = meterRegistry.find(k.id()).meters();

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
            case COUNTER -> Double.valueOf(((Counter) meter).count()).longValue();
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
