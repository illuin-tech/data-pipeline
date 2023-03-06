package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.step.builder.StepDescriptor;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepMetrics
{
    private final Timer runTimer;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineStepMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline, StepDescriptor<?, ?, ?> descriptor)
    {
        this.runTimer = meterRegistry.timer(MeterRegistryKeys.PIPELINE_STEP_RUN_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
        this.successCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RUN_SUCCESS_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
        this.failureCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_STEP_RUN_FAILURE_KEY, "pipeline", pipeline.id(), "step", descriptor.id());
    }

    public Timer runTimer()
    {
        return this.runTimer;
    }

    public Counter successCounter()
    {
        return this.successCounter;
    }

    public Counter failureCounter()
    {
        return this.failureCounter;
    }
}
