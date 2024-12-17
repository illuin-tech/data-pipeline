package tech.illuin.pipeline.observer.descriptor.model;

import java.util.List;
import java.util.Map;

public record PipelineDescription(
    String id,
    InitializerDescription init,
    List<StepDescription> steps,
    List<SinkDescription> sinks,
    Map<String, Metric> metrics
) {}
