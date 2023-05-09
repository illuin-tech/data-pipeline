package tech.illuin.pipeline.step.execution.wrapper.circuitbreaker;

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
