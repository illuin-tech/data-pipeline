package tech.illuin.pipeline.observer.descriptor.model;

import io.micrometer.core.instrument.Meter;

import java.util.Map;

public record Metric(
    String tag,
    Map<Meter.Id, Number> values
) {}
