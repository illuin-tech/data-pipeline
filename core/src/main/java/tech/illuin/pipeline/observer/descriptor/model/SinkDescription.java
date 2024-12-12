package tech.illuin.pipeline.observer.descriptor.model;

import java.util.Map;

public record SinkDescription(
    String id,
    Object sink,
    boolean isAsync,
    Object executionWrapper,
    Object errorHandler,
    Map<String, Metric> metrics
) {}
