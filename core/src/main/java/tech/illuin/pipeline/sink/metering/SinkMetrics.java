package tech.illuin.pipeline.sink.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import tech.illuin.pipeline.metering.BaseMetrics;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.metering.mdc.MDCManager;

import java.util.Collection;

import static tech.illuin.pipeline.metering.MeterRegistryKey.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkMetrics extends BaseMetrics
{
    public SinkMetrics(MeterRegistry meterRegistry, SinkMarkerManager markerManager)
    {
        super(meterRegistry, markerManager);
    }

    public SinkMetrics(MeterRegistry meterRegistry, SinkMarkerManager markerManager, MDCManager mdc)
    {
        super(meterRegistry, markerManager, mdc);
    }

    @Override
    protected ConstantMeters initializeConstantMeters(MeterRegistry meterRegistry, MarkerManager markerManager)
    {
        Collection<Tag> meterTags = this.markerManager.tags();
        return new ConstantMeters(
            meterRegistry.timer(PIPELINE_SINK_RUN_KEY.id(), fill(PIPELINE_SINK_RUN_KEY, meterTags)),
            meterRegistry.counter(PIPELINE_SINK_RUN_TOTAL_KEY.id(), fill(PIPELINE_SINK_RUN_TOTAL_KEY, meterTags)),
            meterRegistry.counter(PIPELINE_SINK_RUN_SUCCESS_KEY.id(), fill(PIPELINE_SINK_RUN_SUCCESS_KEY, meterTags)),
            meterRegistry.counter(PIPELINE_SINK_RUN_FAILURE_KEY.id(), fill(PIPELINE_SINK_RUN_FAILURE_KEY, meterTags))
        );
    }

    @Override
    public Counter errorCounter(Exception exception)
    {
        return this.meterRegistry.counter(
            PIPELINE_SINK_ERROR_TOTAL_KEY.id(),
            fill(
                PIPELINE_SINK_ERROR_TOTAL_KEY,
                this.markerManager.tags(Tag.of("error", exception.getClass().getName()))
            )
        );
    }
}
