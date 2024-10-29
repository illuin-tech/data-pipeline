package tech.illuin.pipeline.step.execution.interruption;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.step.result.Result;

public interface Interruption extends Result
{
    ComponentTag tag();
    Context context();
    String message();

    static Interruption of(ComponentTag tag, Context context, String message)
    {
        return new SimpleInterruption(tag, context, message);
    }
}
