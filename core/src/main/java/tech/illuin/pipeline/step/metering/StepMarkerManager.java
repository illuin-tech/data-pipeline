package tech.illuin.pipeline.step.metering;

import io.micrometer.core.instrument.Tag;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentTag;

import java.util.Map;
import java.util.Set;

import static tech.illuin.pipeline.metering.MetricFunctions.combine;

public class StepMarkerManager implements MarkerManager
{
    private final ComponentTag tag;
    private final MetricTags metricTags;

    public StepMarkerManager(ComponentTag tag, MetricTags metricTags)
    {
        this.tag = tag;
        this.metricTags = metricTags;
    }

    @Override
    public Set<Tag> discriminants()
    {
        return Set.of(
            Tag.of("pipeline", this.tag.pipelineTag().pipeline()),
            Tag.of("step", this.tag.id())
        );
    }

    @Override
    public Set<Tag> tags()
    {
        return combine(this.discriminants(), this.metricTags.asTags());
    }

    @Override
    public Map<String, String> markers()
    {
        return Map.of("step", this.tag.id());
    }
}
