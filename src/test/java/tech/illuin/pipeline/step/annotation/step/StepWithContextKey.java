package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithContextKey
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithContextKey.class);

    @StepConfig(id = "step-with_context-key")
    public Result execute(@Context("some-metadata") String metadata)
    {
        logger.info("context-key: {}", metadata);
        return new TestResult("annotation-test", "single(" + metadata + ")");
    }
}
