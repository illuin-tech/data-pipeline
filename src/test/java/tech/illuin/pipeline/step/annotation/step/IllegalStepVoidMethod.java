package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.step.annotation.StepConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalStepVoidMethod<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalStepVoidMethod.class);

    @StepConfig
    public void execute(@Input T data)
    {
        logger.info("input: {}", data);
    }
}
