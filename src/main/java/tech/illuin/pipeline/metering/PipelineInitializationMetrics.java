package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializationMetrics
{
    private final Timer runTimer;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineInitializationMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline)
    {
        this.runTimer = meterRegistry.timer(MeterRegistryKeys.PIPELINE_INITIALIZATION_RUN_KEY, "pipeline", pipeline.id());
        this.successCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY, "pipeline", pipeline.id());
        this.failureCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_INITIALIZATION_RUN_FAILURE_KEY, "pipeline", pipeline.id());
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
