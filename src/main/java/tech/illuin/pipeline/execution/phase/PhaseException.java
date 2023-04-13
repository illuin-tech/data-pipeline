package tech.illuin.pipeline.execution.phase;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PhaseException extends Exception
{
    public PhaseException(String message)
    {
        super(message);
    }

    public PhaseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PhaseException(Throwable cause)
    {
        super(cause);
    }
}
