package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepReturnsOptionalResult
{
    private static final Logger logger = LoggerFactory.getLogger(StepReturnsOptionalResult.class);

    @StepConfig(id = "step-returns_optional-result")
    public Optional<Result> execute()
    {
        logger.info("optional-result");
        return Optional.of(
            new TestResult("annotation-test", "a")
        );
    }
}
