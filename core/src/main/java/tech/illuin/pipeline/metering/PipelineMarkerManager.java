package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Tag;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static tech.illuin.pipeline.metering.MetricFunctions.combine;

public class PipelineMarkerManager implements MarkerManager
{
    private final PipelineTag tag;
    private final MetricTags metricTags;

    public PipelineMarkerManager(PipelineTag tag, MetricTags metricTags)
    {
        this.tag = tag;
        this.metricTags = metricTags;
    }

    @Override
    public Set<Tag> discriminants()
    {
        return Set.of(
            Tag.of("pipeline", this.tag.pipeline())
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
        Map<String, String> markers = new HashMap<>();
        markers.put("pipeline", this.tag.pipeline());
        /* The author value can be null, thus Map.of cannot be used here */
        markers.put("author", this.tag.author());
        return MetricFunctions.compileMarkers(
            this.metricTags,
            markers,
            emptyMap()
        );
    }
}
