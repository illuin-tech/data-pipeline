package tech.illuin.pipeline.generic.pipeline.execution.error;

import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class WrapAll implements StepErrorHandler
{
    @Override
    public Result handle(Exception exception, Context<?> context)
    {
        return new TestResult("error", "ko");
    }
}
