package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepReturnsMultiResult
{
    private static final Logger logger = LoggerFactory.getLogger(StepReturnsMultiResult.class);

    @StepConfig(id = "step-returns_multi-result")
    public List<Result> execute()
    {
        logger.info("multi-result");
        return List.of(
            new TestResult("annotation-test", "a"),
            new TestResult("annotation-test", "b")
        );
    }
}
