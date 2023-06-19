package tech.illuin.pipeline.sink.execution.wrapper;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class SinkWrapperException extends Exception
{
    public SinkWrapperException(Exception ex)
    {
        super(ex.getMessage(), ex);
    }
}
