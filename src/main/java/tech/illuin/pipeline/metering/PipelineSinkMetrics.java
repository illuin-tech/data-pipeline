package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.sink.SinkDescriptor;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkMetrics
{
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineSinkMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline, SinkDescriptor<?> descriptor)
    {
        this.runTimer = meterRegistry.timer(MeterRegistryKeys.PIPELINE_SINK_RUN_KEY, "pipeline", pipeline.id(), "sink", descriptor.id());
        this.totalCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_SINK_RUN_TOTAL_KEY, "pipeline", pipeline.id(), "sink", descriptor.id());
        this.successCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_SINK_RUN_SUCCESS_KEY, "pipeline", pipeline.id(), "sink", descriptor.id());
        this.failureCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_SINK_RUN_FAILURE_KEY, "pipeline", pipeline.id(), "sink", descriptor.id());
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
}
