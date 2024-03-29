package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithInputAndLatest<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithInputAndLatest.class);

    @StepConfig(id = "step-with_input+latest")
    public Result execute(@Input T data, @Latest TestResult result)
    {
        logger.info("input: {} latest: {}", data, result.status());
        return new TestResult("annotation-test", result.status() + "->single(" + data + ")");
    }

    public static class Named<T>
    {
        @StepConfig(id = "step-with_input+latest")
        public Result execute(@Input T data, @Latest(name = "annotation-named") TestResult result)
        {
            logger.info("input: {} latest: {}", data, result.status());
            return new TestResult("annotation-test", result.status() + "->single(" + data + ")");
        }
    }
}
