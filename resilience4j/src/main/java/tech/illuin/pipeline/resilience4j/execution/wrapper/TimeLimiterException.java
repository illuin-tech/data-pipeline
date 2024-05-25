package tech.illuin.pipeline.resilience4j.execution.wrapper;

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
