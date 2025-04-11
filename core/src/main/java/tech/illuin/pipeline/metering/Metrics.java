package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

public interface Metrics
{
    Timer runTimer();

    Counter totalCounter();

    Counter successCounter();

    Counter failureCounter();

    Counter errorCounter(Exception exception);
}
