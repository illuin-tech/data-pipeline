package tech.illuin.pipeline.step.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface StepErrorHandler
{
    Result handle(Exception exception, Context<?> context) throws StepException;

    StepErrorHandler RETHROW_ALL = StepErrorHandler::rethrowAll;

    private static Result rethrowAll(Exception ex, Context<?> ctx) throws StepException
    {
        if (ex instanceof StepException stepEx)
            throw stepEx;
        if (ex instanceof RuntimeException runEx)
            throw runEx;
        throw new RuntimeException("Unexpected error type found in handler: " + ex.getClass().getName() + " with message " + ex.getMessage());
    }
}
