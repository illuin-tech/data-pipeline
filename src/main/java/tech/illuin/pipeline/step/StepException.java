package tech.illuin.pipeline.step;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepException extends Exception
{
    public StepException(String message)
    {
        super(message);
    }

    public StepException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
