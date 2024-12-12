package tech.illuin.pipeline.observer.descriptor.model;

import java.util.Map;

public record InitializerDescription(
    String id,
    Object initializer,
    Object errorHandler,
    Map<String, Metric> metrics
) {}
