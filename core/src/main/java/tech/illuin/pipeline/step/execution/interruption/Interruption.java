package tech.illuin.pipeline.step.execution.interruption;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.step.result.Result;

public record Interruption(
    ComponentTag tag,
    Context context,
    String message
) implements Result {}
