package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.marker.DefaultMDCManager;
import tech.illuin.pipeline.metering.marker.MDCManager;
import tech.illuin.pipeline.metering.marker.MDCMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentTag;

import java.util.Collection;
import java.util.Map;

import static tech.illuin.pipeline.metering.MeterRegistryKey.*;
import static tech.illuin.pipeline.metering.MetricFunctions.compileAndFillTags;
import static tech.illuin.pipeline.metering.MetricFunctions.compileTags;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializationMetrics extends MDCMarker
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
        this(meterRegistry, tag, metricTags, new DefaultMDCManager());
    }

    public PipelineInitializationMetrics(MeterRegistry meterRegistry, ComponentTag tag, MetricTags metricTags, MDCManager mdc)
    {
        super(mdc);
        this.meterRegistry = meterRegistry;
        this.tag = tag;
        this.metricTags = metricTags;
        Tag[] discriminants = computeDiscriminants(this.tag.pipelineTag().pipeline(), this.tag.id());
        Collection<Tag> meterTags = compileTags(this.metricTags, discriminants);
        this.runTimer = meterRegistry.timer(PIPELINE_INITIALIZATION_RUN_KEY.id(), fill(PIPELINE_INITIALIZATION_RUN_KEY, meterTags));
        this.totalCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_TOTAL_KEY.id(), fill(PIPELINE_INITIALIZATION_RUN_TOTAL_KEY, meterTags));
        this.successCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY.id(), fill(PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY, meterTags));
        this.failureCounter = meterRegistry.counter(PIPELINE_INITIALIZATION_RUN_FAILURE_KEY.id(), fill(PIPELINE_INITIALIZATION_RUN_FAILURE_KEY, meterTags));
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
            PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY.id(),
            compileAndFillTags(
                this.metricTags,
                PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY,
                Tag.of("pipeline", this.tag.pipelineTag().pipeline()),
                Tag.of("initializer", this.tag.id()),
                Tag.of("error", exception.getClass().getName())
            )
        );
    }

    public static Tag[] computeDiscriminants(String pipeline, String identifier)
    {
        return new Tag[]{
            Tag.of("pipeline", pipeline),
            Tag.of("initializer", identifier),
        };
    }

    @Override
    public Map<String, String> compileMarkers()
    {
        return Map.of("initializer", this.tag.id());
    }
}
