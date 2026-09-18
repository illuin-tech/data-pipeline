package tech.illuin.pipeline.observer.descriptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.metering.MeterRegistryKey;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.observer.descriptor.describable.DefaultDescribable;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.observer.descriptor.describable.Description;
import tech.illuin.pipeline.observer.descriptor.model.*;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.*;
import java.util.stream.Collectors;

import static tech.illuin.pipeline.metering.MeterRegistryKey.*;

public class DescriptorObserver implements Observer
{
    private static final List<MeterRegistryKey> PIPELINE_KEYS = List.of(
        PIPELINE_RUN_KEY,
        PIPELINE_RUN_TOTAL_KEY,
        PIPELINE_RUN_SUCCESS_KEY,
        PIPELINE_RUN_FAILURE_KEY,
        PIPELINE_RUN_ERROR_TOTAL_KEY
    );

    private static final List<MeterRegistryKey> INITIALIZATION_KEYS = List.of(
        PIPELINE_INITIALIZATION_RUN_KEY,
        PIPELINE_INITIALIZATION_RUN_TOTAL_KEY,
        PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY,
        PIPELINE_INITIALIZATION_RUN_FAILURE_KEY,
        PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY
    );

    private static final List<MeterRegistryKey> STEP_KEYS = List.of(
        PIPELINE_STEP_RUN_KEY,
        PIPELINE_STEP_RUN_TOTAL_KEY,
        PIPELINE_STEP_RUN_SUCCESS_KEY,
        PIPELINE_STEP_RUN_FAILURE_KEY,
        PIPELINE_STEP_RESULT_TOTAL_KEY,
        PIPELINE_STEP_ERROR_TOTAL_KEY
    );

    private static final List<MeterRegistryKey> SINK_KEYS = List.of(
        PIPELINE_SINK_RUN_KEY,
        PIPELINE_SINK_RUN_TOTAL_KEY,
        PIPELINE_SINK_RUN_SUCCESS_KEY,
        PIPELINE_SINK_RUN_FAILURE_KEY,
        PIPELINE_SINK_ERROR_TOTAL_KEY
    );

    private static final Set<String> ALL_KEYS = java.util.stream.Stream.of(
        PIPELINE_KEYS,
        INITIALIZATION_KEYS,
        STEP_KEYS,
        SINK_KEYS
    ).flatMap(Collection::stream).map(MeterRegistryKey::id).collect(Collectors.toUnmodifiableSet());

    private String id;
    private MeterRegistry meterRegistry;
    private InitializerTemplate initTemplate;
    private List<StepTemplate> stepTemplates;
    private List<SinkTemplate> sinkTemplates;
    private boolean initialized = false;

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
        this.id = id;
        this.meterRegistry = meterRegistry;
        this.initTemplate = new InitializerTemplate(
            initializer.id(),
            compileDescription(initializer.initializer()),
            compileDescription(initializer.errorHandler())
        );

        this.stepTemplates = steps.stream()
            .map(sd -> new StepTemplate(
                sd.id(),
                compileDescription(sd.step()),
                sd.isPinned(),
                compileDescription(sd.executionWrapper()),
                compileDescription(sd.activationPredicate()),
                compileDescription(sd.resultEvaluator()),
                compileDescription(sd.errorHandler())
            ))
            .toList();

        this.sinkTemplates = sinks.stream()
            .map(sd -> new SinkTemplate(
                sd.id(),
                compileDescription(sd.sink()),
                sd.isAsync(),
                compileDescription(sd.executionWrapper()),
                compileDescription(sd.errorHandler())
            ))
            .toList();

        this.initialized = true;
    }

    private static Object compileDescription(Object property)
    {
        if (property instanceof Describable describable)
            return compileDescription(describable.describe());
        if (property instanceof Description description)
            return description;
        return new DefaultDescribable(property).describe();
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

    private static Map<String, Map<Id, Number>> initMetricMap(List<MeterRegistryKey> keys)
    {
        Map<String, Map<Id, Number>> map = new HashMap<>();
        for (MeterRegistryKey key : keys)
            map.put(key.id(), new HashMap<>());
        return map;
    }

    private static Map<String, Metric> toMetrics(Map<String, Map<Id, Number>> metricMap)
    {
        return metricMap.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> new Metric(
                e.getKey(),
                e.getValue().isEmpty()
                    ? Collections.emptyMap()
                    : Collections.unmodifiableMap(e.getValue())
            )
        ));
    }

    public PipelineDescription describe()
    {
        if (!this.initialized)
            throw new IllegalStateException("The observer has not been initialized");

        Map<String, Map<Id, Number>> pipelineMetricValues = initMetricMap(PIPELINE_KEYS);
        Map<String, Map<Id, Number>> initMetricValues = initMetricMap(INITIALIZATION_KEYS);

        Map<String, Map<String, Map<Id, Number>>> stepMetricValues = new HashMap<>();
        for (StepTemplate st : this.stepTemplates)
            stepMetricValues.put(st.id(), initMetricMap(STEP_KEYS));

        Map<String, Map<String, Map<Id, Number>>> sinkMetricValues = new HashMap<>();
        for (SinkTemplate st : this.sinkTemplates)
            sinkMetricValues.put(st.id(), initMetricMap(SINK_KEYS));

        for (Meter meter : this.meterRegistry.getMeters())
        {
            Id meterId = meter.getId();
            String metricName = meterId.getName();
            if (!ALL_KEYS.contains(metricName))
                continue;

            String pipelineTag = meterId.getTag("pipeline");
            if (!this.id.equals(pipelineTag))
                continue;
            String stepTag = meterId.getTag("step");
            if (stepTag != null)
            {
                Map<String, Map<Id, Number>> stepMap = stepMetricValues.get(stepTag);
                if (stepMap != null)
                {
                    Map<Id, Number> values = stepMap.get(metricName);
                    if (values != null)
                        values.put(meterId, getValue(meter));
                }
                continue;
            }

            String sinkTag = meterId.getTag("sink");
            if (sinkTag != null)
            {
                Map<String, Map<Id, Number>> sinkMap = sinkMetricValues.get(sinkTag);
                if (sinkMap != null)
                {
                    Map<Id, Number> values = sinkMap.get(metricName);
                    if (values != null)
                        values.put(meterId, getValue(meter));
                }
                continue;
            }

            String initTag = meterId.getTag("initializer");
            if (initTag != null)
            {
                if (this.initTemplate.id().equals(initTag))
                {
                    Map<Id, Number> values = initMetricValues.get(metricName);
                    if (values != null)
                        values.put(meterId, getValue(meter));
                }
                continue;
            }

            Map<Id, Number> values = pipelineMetricValues.get(metricName);
            if (values != null)
                values.put(meterId, getValue(meter));
        }

        InitializerDescription initDesc = new InitializerDescription(
            this.initTemplate.id(),
            this.initTemplate.initializerDesc(),
            this.initTemplate.errorHandlerDesc(),
            toMetrics(initMetricValues)
        );

        List<StepDescription> stepDescs = this.stepTemplates.stream()
            .map(st -> new StepDescription(
                st.id(),
                st.stepDesc(),
                st.isPinned(),
                st.executionWrapperDesc(),
                st.activationPredicateDesc(),
                st.resultEvaluatorDesc(),
                st.errorHandlerDesc(),
                toMetrics(stepMetricValues.get(st.id()))
            ))
            .toList();

        List<SinkDescription> sinkDescs = this.sinkTemplates.stream()
            .map(st -> new SinkDescription(
                st.id(),
                st.sinkDesc(),
                st.isAsync(),
                st.executionWrapperDesc(),
                st.errorHandlerDesc(),
                toMetrics(sinkMetricValues.get(st.id()))
            ))
            .toList();

        return new PipelineDescription(
            this.id,
            initDesc,
            stepDescs,
            sinkDescs,
            toMetrics(pipelineMetricValues)
        );
    }

    @Override
    public void close() {}

    private record InitializerTemplate(
        String id,
        Object initializerDesc,
        Object errorHandlerDesc
    ) {}

    private record StepTemplate(
        String id,
        Object stepDesc,
        boolean isPinned,
        Object executionWrapperDesc,
        Object activationPredicateDesc,
        Object resultEvaluatorDesc,
        Object errorHandlerDesc
    ) {}

    private record SinkTemplate(
        String id,
        Object sinkDesc,
        boolean isAsync,
        Object executionWrapperDesc,
        Object errorHandlerDesc
    ) {}
}
