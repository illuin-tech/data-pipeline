package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithResults
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithResults.class);

    @StepConfig(id = "step-with_results")
    public Result execute(Results results)
    {
        logger.info("results: {}", results);
        return new TestResult("annotation-test", results.latest(TestResult.class).map(TestResult::status).orElse(null));
    }
}
