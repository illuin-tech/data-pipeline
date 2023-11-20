package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithInputAndLatestOptional<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithInputAndLatestOptional.class);

    @StepConfig(id = "step-with_input+latest-optional")
    public Result execute(@Input T data, @Latest Optional<TestResult> result)
    {
        String status = result.map(TestResult::status).orElse(null);
        logger.info("input: {} latest: {}", data, status);
        return new TestResult("annotation-test", status + "->" + data);
    }

    public static class Named<T>
    {
        @StepConfig(id = "step-with_input+latest-optional")
        public Result execute(@Input T data, @Latest(name = "annotation-named") Optional<TestResult> result)
        {
            String status = result.map(TestResult::status).orElse(null);
            logger.info("input: {} latest: {}", data, status);
            return new TestResult("annotation-test", status + "->optional(" + data + ")");
        }
    }
}
