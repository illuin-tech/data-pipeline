package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.marker.DefaultMDCManager;
import tech.illuin.pipeline.metering.marker.MDCManager;
import tech.illuin.pipeline.metering.marker.MDCMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static tech.illuin.pipeline.metering.MeterRegistryKey.*;
import static tech.illuin.pipeline.metering.MetricFunctions.compileAndFillTags;
import static tech.illuin.pipeline.metering.MetricFunctions.compileTags;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineMetrics extends MDCMarker
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
        this(meterRegistry, tag, metricTags, new DefaultMDCManager());
    }

    public PipelineMetrics(MeterRegistry meterRegistry, PipelineTag tag, MetricTags metricTags, MDCManager mdc)
    {
        super(mdc);
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

    public static Tag[] computeDiscriminants(String identifier)
    {
        return new Tag[]{Tag.of("pipeline", identifier)};
    }

    @Override
    public Map<String, String> compileMarkers()
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
