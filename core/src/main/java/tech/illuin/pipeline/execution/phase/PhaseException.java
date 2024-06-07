package tech.illuin.pipeline.execution.phase;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 * @deprecated slated for removal by 0.12
 */
@Deprecated
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
