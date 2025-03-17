package tech.illuin.pipeline.metering.manager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;

public interface ObservabilityManager
{
    MeterRegistry meterRegistry();

    Tracer tracer();

    interface Builder
    {
        MeterRegistry meterRegistry();

        void setMeterRegistry(MeterRegistry meterRegistry);

        Tracer tracer();

        void setTracer(Tracer tracer);

        ObservabilityManager build();
    }
}
