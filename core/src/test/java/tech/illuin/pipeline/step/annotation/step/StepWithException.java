package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithException<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithException.class);

    @StepConfig(id = "step-with_input+exception")
    public Result execute(@Input T data) throws Exception
    {
        logger.info("input: {}", data);
        throw new StepWithExceptionException("This is an exception for input " + data);
    }

    public static class StepWithExceptionException extends Exception
    {
        public StepWithExceptionException(String message)
        {
            super(message);
        }
    }
}
