package tech.illuin.pipeline.generic.pipeline.step;

import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record TestResult(
    String name,
    String status
) implements Result {}
