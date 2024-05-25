package tech.illuin.pipeline.step.execution.wrapper;


/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class StepWrapperException extends Exception
{
    public StepWrapperException(Exception ex)
    {
        super(ex.getMessage(), ex);
    }
}
