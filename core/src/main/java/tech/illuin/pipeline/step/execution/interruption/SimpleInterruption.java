package tech.illuin.pipeline.step.execution.interruption;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.ComponentTag;

record SimpleInterruption(
    ComponentTag tag,
    Context context,
    String message
) implements Interruption {}
