package tech.illuin.pipeline.metering;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static tech.illuin.pipeline.metering.MeterRegistryKey.*;
import static tech.illuin.pipeline.metering.MetricFunctions.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineMetrics implements LogMarker
{
    private final MeterRegistry meterRegistry;
    private final PipelineTag tag;
    private final MetricTags metricTags;
    private final Timer runTimer;
    private final Counter totalCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public PipelineMetrics(MeterRegistry meterRegistry, PipelineTag tag, MetricTags metricTags)
    {
        this.meterRegistry = meterRegistry;
        this.tag = tag;
        this.metricTags = metricTags;
        Tag[] discriminants = computeDiscriminants(tag.pipeline());
        Collection<Tag> meterTags = compileTags(this.metricTags, discriminants);
        this.runTimer = meterRegistry.timer(PIPELINE_RUN_KEY.id(), fill(PIPELINE_RUN_KEY, meterTags));
        this.totalCounter = meterRegistry.counter(PIPELINE_RUN_TOTAL_KEY.id(), fill(PIPELINE_RUN_TOTAL_KEY, meterTags));
        this.successCounter = meterRegistry.counter(PIPELINE_RUN_SUCCESS_KEY.id(), fill(PIPELINE_RUN_SUCCESS_KEY, meterTags));
        this.failureCounter = meterRegistry.counter(PIPELINE_RUN_FAILURE_KEY.id(), fill(PIPELINE_RUN_FAILURE_KEY, meterTags));
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
            PIPELINE_RUN_ERROR_TOTAL_KEY.id(),
            compileAndFillTags(
                this.metricTags,
                PIPELINE_RUN_ERROR_TOTAL_KEY,
                Tag.of("pipeline", this.tag.pipeline()),
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
                "pipeline", this.tag.pipeline(),
                "author", this.tag.author()
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
                "pipeline", this.tag.pipeline(),
                "author", this.tag.author(),
                "error", exception.getClass().getName()
            ),
            emptyMap()
        ));
    }

    public static Tag[] computeDiscriminants(String identifier)
    {
        return new Tag[]{Tag.of("pipeline", identifier)};
    }
}
