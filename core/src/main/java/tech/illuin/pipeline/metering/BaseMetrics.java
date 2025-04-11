package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import tech.illuin.pipeline.metering.mdc.DefaultMDCManager;
import tech.illuin.pipeline.metering.mdc.MDCManager;

import java.util.stream.Stream;

public abstract class BaseMetrics implements Metrics
{
    protected final MeterRegistry meterRegistry;
    protected final MarkerManager markerManager;
    protected final MDCManager mdc;
    private final ConstantMeters constantMeters;

    public BaseMetrics(MeterRegistry meterRegistry, MarkerManager markerManager)
    {
        this(meterRegistry, markerManager, new DefaultMDCManager());
    }

    public BaseMetrics(MeterRegistry meterRegistry, MarkerManager markerManager, MDCManager mdc)
    {
        this.meterRegistry = meterRegistry;
        this.markerManager = markerManager;
        this.mdc = mdc;
        this.constantMeters = this.initializeConstantMeters(meterRegistry, markerManager);
    }

    public final void setMDC()
    {
        this.markerManager.markers().forEach(this.mdc::put);
    }

    public final void setMDC(Exception exception)
    {
        this.mdc.put("error", exception.getClass().getName());
    }

    public final void unsetMDC()
    {
        Stream.concat(
            this.markerManager.markers().keySet().stream(),
            Stream.of("error")
        ).forEach(this.mdc::remove);
    }

    protected abstract ConstantMeters initializeConstantMeters(MeterRegistry meterRegistry, MarkerManager markerManager);

    @Override
    public Timer runTimer()
    {
        return this.constantMeters.runTimer();
    }

    @Override
    public Counter totalCounter()
    {
        return this.constantMeters.totalCounter();
    }

    @Override
    public Counter successCounter()
    {
        return this.constantMeters.successCounter();
    }

    @Override
    public Counter failureCounter()
    {
        return this.constantMeters.failureCounter();
    }

    public record ConstantMeters(
        Timer runTimer,
        Counter totalCounter,
        Counter successCounter,
        Counter failureCounter
    ) {}
}
