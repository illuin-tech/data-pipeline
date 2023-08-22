package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithUIDGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithUIDGenerator.class);

    @StepConfig(id = "step-with_uid-generator")
    public Result execute(UIDGenerator generator)
    {
        logger.info("uid-generator: {}", generator);
        return new TestResult("annotation-test", generator.getClass().getSimpleName());
    }
}
