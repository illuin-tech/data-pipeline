package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentTag;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static tech.illuin.pipeline.metering.MeterRegistryKeys.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializationMetrics implements LogMarker
{
    private final MeterRegistry meterRegistry;
    private final ComponentTag tag;
    private final MetricTags metricTags;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineInitializationMetrics(MeterRegistry meterRegistry, ComponentTag tag, MetricTags metricTags)
    {
        this.meterRegistry = meterRegistry;
        this.tag = tag;
        this.metricTags = metricTags;
        Collection<Tag> meterTags = this.compileTags(Tag.of("pipeline", this.tag.pipelineTag().pipeline()), Tag.of("initializer", this.tag.id()));
        this.runTimer = meterRegistry.timer(PIPELINE_INITIALIZATION_RUN_KEY, meterTags);
        this.totalCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_TOTAL_KEY, meterTags);
        this.successCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY, meterTags);
        this.failureCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_FAILURE_KEY, meterTags);
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
        return this.meterRegistry.counter(
            MeterRegistryKeys.PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY,
            this.compileTags(
                Tag.of("pipeline", this.tag.pipelineTag().pipeline()),
                Tag.of("initializer", this.tag.id()),
                Tag.of("error", exception.getClass().getName())
            )
        );
    }

    @Override
    public LabelMarker mark(Map<String, String> labels)
    {
        return LabelMarker.of(() -> this.compileMarkers(
            Map.of(
                "pipeline", this.tag.pipelineTag().pipeline(),
                "author", this.tag.pipelineTag().author(),
                "initializer", this.tag.id()
            ),
            labels
        ));
    }

    @Override
    public LabelMarker mark(Exception exception)
    {
        return LabelMarker.of(() -> this.compileMarkers(Map.of(
            "pipeline", this.tag.pipelineTag().pipeline(),
            "author", this.tag.pipelineTag().author(),
            "error", exception.getClass().getName(),
            "initializer", this.tag.id()
        ), emptyMap()));
    }

    private Collection<Tag> compileTags(Tag... mainstayTags)
    {
        return MetricFunctions.combine(List.of(mainstayTags), this.metricTags.asTags());
    }

    private Map<String, String> compileMarkers(Map<String, String> mainstayMarkers, Map<String, String> dynamicMarkers)
    {
        return MetricFunctions.combine(mainstayMarkers, this.metricTags.asMap(), dynamicMarkers);
    }
}
