package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkMetrics
{
    private final String sinkId;
    private final MeterRegistry meterRegistry;
    private final Pipeline<?, ?> pipeline;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineSinkMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline, SinkDescriptor<?> descriptor)
    {
        this.sinkId = descriptor.id();
        this.meterRegistry = meterRegistry;
        this.pipeline = pipeline;
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

    public Counter errorCounter(Exception exception)
    {
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_SINK_ERROR_TOTAL_KEY, "pipeline", this.pipeline.id(), "sink", this.sinkId, "error", exception.getClass().getName());
    }

    public LabelMarker marker(PipelineTag tag)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "sink", this.sinkId
        ));
    }

    public LabelMarker marker(PipelineTag tag, Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "error", exception.getClass().getName(),
            "sink", this.sinkId
        ));
    }
}
