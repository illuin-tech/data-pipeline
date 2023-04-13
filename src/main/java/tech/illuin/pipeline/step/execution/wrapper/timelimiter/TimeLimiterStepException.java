package tech.illuin.pipeline.step.execution.wrapper.timelimiter;

import tech.illuin.pipeline.step.StepException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterStepException extends RuntimeException
{
    public TimeLimiterStepException(String message, StepException ex)
    {
        super(message, ex);
    }

    @Override
    public StepException getCause()
    {
        return (StepException) super.getCause();
    }
}
