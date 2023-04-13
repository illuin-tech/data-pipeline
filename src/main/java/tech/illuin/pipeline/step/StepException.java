package tech.illuin.pipeline.step;

import tech.illuin.pipeline.execution.phase.PhaseException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepException extends PhaseException
{
    public StepException(String message)
    {
        super(message);
    }

    public StepException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public StepException(Throwable cause)
    {
        super(cause);
    }
}
