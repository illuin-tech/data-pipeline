package tech.illuin.pipeline.step.execution.wrapper.retry;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryException extends RuntimeException
{
    public RetryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
