package tech.illuin.pipeline.step.runner;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepRunnerException extends Exception
{
    public StepRunnerException(String message)
    {
        super(message);
    }

    public StepRunnerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
