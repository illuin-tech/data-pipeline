package tech.illuin.pipeline.generic.pipeline.step.execution.error;

import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class WrapAll implements StepErrorHandler
{
    @Override
    public Result handle(Exception exception, Object input, Object payload, Results results, LocalContext context)
    {
        return new TestResult("error", "ko");
    }
}
