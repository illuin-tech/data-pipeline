package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Map;

import static tech.illuin.pipeline.metering.MeterRegistryKeys.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializationMetrics
{
    private final String initializerId;
    private final MeterRegistry meterRegistry;
    private final Pipeline<?, ?> pipeline;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineInitializationMetrics(MeterRegistry meterRegistry, Pipeline<?, ?> pipeline, InitializerDescriptor<?, ?> descriptor)
    {
        this.initializerId = descriptor.id();
        this.meterRegistry = meterRegistry;
        this.pipeline = pipeline;
        this.runTimer = meterRegistry.timer(PIPELINE_INITIALIZATION_RUN_KEY, "pipeline", pipeline.id());
        this.totalCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_TOTAL_KEY, "pipeline", pipeline.id());
        this.successCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY, "pipeline", pipeline.id());
        this.failureCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_FAILURE_KEY, "pipeline", pipeline.id());
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
        return this.meterRegistry.counter(MeterRegistryKeys.PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY, "pipeline", this.pipeline.id(), "error", exception.getClass().getName());
    }

    public LabelMarker marker(PipelineTag tag)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "initializer", this.initializerId
        ));
    }

    public LabelMarker marker(PipelineTag tag, Exception exception)
    {
        return LabelMarker.of(() -> Map.of(
            "pipeline", tag.pipeline(),
            "author", tag.author(),
            "error", exception.getClass().getName(),
            "initializer", this.initializerId
        ));
    }
}
