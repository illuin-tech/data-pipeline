package tech.illuin.pipeline.observer.descriptor.model;

import java.util.Map;

public record StepDescription(
    String id,
    Object step,
    boolean pinned,
    Object executionWrapper,
    Object condition,
    Object resultEvaluator,
    Object errorHandler,
    Map<String, Metric> metrics
) {}
