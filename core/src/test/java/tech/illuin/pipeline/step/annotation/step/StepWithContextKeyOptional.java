package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithContextKeyOptional
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithContextKeyOptional.class);

    @StepConfig(id = "step-with_context-key-optional")
    public Result execute(@Context("some-metadata") Optional<String> metadata)
    {
        String value = metadata.orElse(null);
        logger.info("context-key: {}", value);
        return new TestResult("annotation-test", "optional(" + value + ")");
    }
}
