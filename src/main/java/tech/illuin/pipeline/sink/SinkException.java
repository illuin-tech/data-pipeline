package tech.illuin.pipeline.sink;

import tech.illuin.pipeline.execution.phase.PhaseException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 * @deprecated slated for removal by 0.12
 */
@Deprecated
public class SinkException extends PhaseException
{
    public SinkException(String message)
    {
        super(message);
    }

    public SinkException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SinkException(Throwable cause)
    {
        super(cause);
    }
}
