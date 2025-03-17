package tech.illuin.pipeline.metering.manager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;

public class DefaultObservabilityManager implements ObservabilityManager
{
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    public DefaultObservabilityManager(MeterRegistry meterRegistry, Tracer tracer)
    {
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
    }

    @Override
    public MeterRegistry meterRegistry()
    {
        return this.meterRegistry;
    }

    @Override
    public Tracer tracer()
    {
        return this.tracer;
    }

    public static class Builder implements ObservabilityManager.Builder
    {
        private MeterRegistry meterRegistry;
        private Tracer tracer;

        public Builder() {}

        @Override
        public MeterRegistry meterRegistry()
        {
            return this.meterRegistry;
        }

        @Override
        public void setMeterRegistry(MeterRegistry meterRegistry)
        {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public Tracer tracer()
        {
            return this.tracer;
        }

        @Override
        public void setTracer(Tracer tracer)
        {
            this.tracer = tracer;
        }

        @Override
        public ObservabilityManager build()
        {
            return new DefaultObservabilityManager(this.meterRegistry, this.tracer);
        }
    }

}
