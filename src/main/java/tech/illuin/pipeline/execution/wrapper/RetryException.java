package tech.illuin.pipeline.execution.wrapper;

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
