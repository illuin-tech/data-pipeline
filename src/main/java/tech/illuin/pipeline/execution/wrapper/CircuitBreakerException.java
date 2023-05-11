package tech.illuin.pipeline.execution.wrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerException extends RuntimeException
{
    public CircuitBreakerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
