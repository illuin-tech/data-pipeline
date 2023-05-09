package tech.illuin.pipeline.step.execution.wrapper.timelimiter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterException extends RuntimeException
{
    public TimeLimiterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
