package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.step.result.Result;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static tech.illuin.pipeline.metering.MeterRegistryKey.*;
import static tech.illuin.pipeline.metering.MetricFunctions.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepMetrics implements LogMarker
{
    private final MeterRegistry meterRegistry;
    private final ComponentTag tag;
    private final MetricTags metricTags;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineStepMetrics(MeterRegistry meterRegistry, ComponentTag tag, MetricTags metricTags)
    {
        this.meterRegistry = meterRegistry;
        this.metricTags = metricTags;
        this.tag = tag;
        Tag[] discriminants = computeDiscriminants(this.tag.pipelineTag().pipeline(), this.tag.id());
        Collection<Tag> meterTags = compileTags(this.metricTags, discriminants);
        this.runTimer = meterRegistry.timer(PIPELINE_STEP_RUN_KEY.id(), fill(PIPELINE_STEP_RUN_KEY, meterTags));
        this.totalCounter = meterRegistry.counter(PIPELINE_STEP_RUN_TOTAL_KEY.id(), fill(PIPELINE_STEP_RUN_TOTAL_KEY, meterTags));
        this.successCounter = meterRegistry.counter(PIPELINE_STEP_RUN_SUCCESS_KEY.id(), fill(PIPELINE_STEP_RUN_SUCCESS_KEY, meterTags));
        this.failureCounter = meterRegistry.counter(PIPELINE_STEP_RUN_FAILURE_KEY.id(), fill(PIPELINE_STEP_RUN_FAILURE_KEY, meterTags));
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
        return this.meterRegistry.counter(
            PIPELINE_STEP_RESULT_TOTAL_KEY.id(),
            compileAndFillTags(
                this.metricTags,
                PIPELINE_STEP_RESULT_TOTAL_KEY,
                Tag.of("pipeline", this.tag.pipelineTag().pipeline()),
                Tag.of("step", this.tag.id()),
                Tag.of("result", result.name())
            )
        );
    }

    public Counter errorCounter(Exception exception)
    {
        return this.meterRegistry.counter(
            PIPELINE_STEP_ERROR_TOTAL_KEY.id(),
            compileAndFillTags(
                this.metricTags,
                PIPELINE_STEP_ERROR_TOTAL_KEY,
                Tag.of("pipeline", this.tag.pipelineTag().pipeline()),
                Tag.of("step", this.tag.id()),
                Tag.of("error", exception.getClass().getName())
            )
        );
    }

    @Override
    public LabelMarker mark(Map<String, String> labels)
    {
        return LabelMarker.of(() -> compileMarkers(
            this.metricTags,
            Map.of(
                "pipeline", this.tag.pipelineTag().pipeline(),
                "author", this.tag.pipelineTag().author(),
                "step", this.tag.id()
            ),
            labels
        ));
    }

    @Override
    public LabelMarker mark(Exception exception)
    {
        return LabelMarker.of(() -> compileMarkers(
            this.metricTags,
            Map.of(
                "pipeline", this.tag.pipelineTag().pipeline(),
                "author", this.tag.pipelineTag().author(),
                "error", exception.getClass().getName(),
                "step", this.tag.id()
            ),
            emptyMap()
        ));
    }

    public static Tag[] computeDiscriminants(String pipeline, String identifier)
    {
        return new Tag[]{
            Tag.of("pipeline", pipeline),
            Tag.of("step", identifier),
        };
    }
}
