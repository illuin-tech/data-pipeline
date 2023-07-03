package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.result.Result;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepMetrics
{
    private final String stepId;
    private final MeterRegistry meterRegistry;
    private final Pipeline<?, ?> pipeline;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineStepMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline, StepDescriptor<?, ?, ?> descriptor)
    {
        this.stepId = descriptor.id();
        this.meterRegistry = meterRegistry;
        this.pipeline = pipeline;
        this.runTimer = meterRegistry.timer(MeterRegistryKeys.PIPELINE_STEP_RUN_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
        this.totalCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RUN_TOTAL_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
        this.successCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RUN_SUCCESS_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
        this.failureCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RUN_FAILURE_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
    }

    public Timer runTimer()
    {
        return this.runTimer;
    }

    public Counter totalCounter()
    {
        return this.totalCounter;
    }

    public Counter successCounter()
    {
        return this.successCounter;
    }

    public Counter failureCounter()
    {
        return this.failureCounter;
    }

    public Counter resultCounter(Result result)
    {
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RESULT_TOTAL_KEY, "pipeline", this.pipeline.id(), "step", this.stepId, "result", result.name());
    }

    public Counter errorCounter(Exception exception)
    {
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_ERROR_TOTAL_KEY, "pipeline", this.pipeline.id(), "step", this.stepId, "error", exception.getClass().getName());
    }

    public LabelMarker marker(PipelineTag tag)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "step", this.stepId
        ));
    }

    public LabelMarker marker(PipelineTag tag, Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "error", exception.getClass().getName(),
            "step", this.stepId
        ));
    }
}
