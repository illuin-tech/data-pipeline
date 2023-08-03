package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalStepMultipleAnnotations<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalStepMultipleAnnotations.class);

    @StepConfig
    public Result execute(@Input @Object T data)
    {
        logger.info("input-object: {}", data);
        return new TestResult("test-annotation", Objects.toString(data));
    }
}
