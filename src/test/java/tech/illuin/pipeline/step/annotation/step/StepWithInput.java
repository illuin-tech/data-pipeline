package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithInput<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithInput.class);

    @StepConfig(id = "step-with_input")
    public Result execute(@Input T data)
    {
        logger.info("input: {}", data);
        return new TestResult("annotation-test", Objects.toString(data));
    }
}
