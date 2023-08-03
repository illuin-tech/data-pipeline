package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.output.ComponentTag;

import java.util.Map;

import static tech.illuin.pipeline.metering.MeterRegistryKeys.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializationMetrics
{
    private final ComponentTag tag;
    private final MeterRegistry meterRegistry;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineInitializationMetrics(MeterRegistry meterRegistry, ComponentTag tag)
    {
        this.tag = tag;
        this.meterRegistry = meterRegistry;
        this.runTimer = meterRegistry.timer(PIPELINE_INITIALIZATION_RUN_KEY, "pipeline", this.tag.pipelineTag().pipeline());
        this.totalCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_TOTAL_KEY, "pipeline", this.tag.pipelineTag().pipeline());
        this.successCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY, "pipeline", this.tag.pipelineTag().pipeline());
        this.failureCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_FAILURE_KEY, "pipeline", this.tag.pipelineTag().pipeline());
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
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY, "pipeline", this.tag.pipelineTag().pipeline(), "error", exception.getClass().getName());
    }

    public LabelMarker marker()
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", this.tag.pipelineTag().pipeline(),
            "author", this.tag.pipelineTag().author(),
            "initializer", this.tag.id()
        ));
    }

    public LabelMarker marker(Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", this.tag.pipelineTag().pipeline(),
            "author", this.tag.pipelineTag().author(),
            "error", exception.getClass().getName(),
            "initializer", this.tag.id()
        ));
    }
}
