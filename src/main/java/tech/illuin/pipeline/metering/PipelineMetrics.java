package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineMetrics
{
    private final MeterRegistry meterRegistry;
    private final Pipeline<?, ?> pipeline;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline)
    {
        this.meterRegistry = meterRegistry;
        this.pipeline = pipeline;
        this.runTimer = meterRegistry.timer(MeterRegistryKeys.PIPELINE_RUN_KEY, "pipeline", pipeline.id());
        this.totalCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_RUN_TOTAL_KEY, "pipeline", pipeline.id());
        this.successCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_RUN_SUCCESS_KEY, "pipeline", pipeline.id());
        this.failureCounter = meterRegistry.counter(MeterRegistryKeys.PIPELINE_RUN_FAILURE_KEY, "pipeline", pipeline.id());
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
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_RUN_ERROR_TOTAL_KEY, "pipeline", this.pipeline.id(), "error", exception.getClass().getName());
    }

    public LabelMarker marker()
    {
        return LabelMarker.of("pipeline", this.pipeline::id);
    }

    public LabelMarker marker(PipelineTag tag)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author()
        ));
    }

    public LabelMarker marker(Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", this.pipeline.id(),
            "error", exception.getClass().getName()
        ));
    }

    public LabelMarker marker(PipelineTag tag, Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "error", exception.getClass().getName()
        ));
    }
}
