package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithInputAndCurrent<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithInputAndCurrent.class);

    @StepConfig(id = "step-with_input+current")
    public Result execute(@Input T data, @Current TestResult result)
    {
        logger.info("input: {} current: {}", data, result.status());
        return new TestResult("annotation-test", result.status() + "->single(" + data +")");
    }

    public static class Required<T>
    {
        @StepConfig(id = "step-with_input+current")
        public Result execute(@Input T data, @Current(required = true) TestResult result)
        {
            logger.info("input: {} current: {}", data, result.status());
            return new TestResult("annotation-test", result.status() + "->single(" + data + ")");
        }
    }

    public static class Named<T>
    {
        @StepConfig(id = "step-with_input+current")
        public Result execute(@Input T data, @Current(name = "annotation-named") TestResult result)
        {
            logger.info("input: {} current: {}", data, result.status());
            return new TestResult("annotation-test", result.status() + "->single(" + data + ")");
        }
    }

    public static class Self<T>
    {
        @StepConfig(id = "step-with_input+current")
        public Result execute(@Input T data, @Current(self = true) TestResult result)
        {
            logger.info("input: {} current: {}", data, result.status());
            return new TestResult("annotation-test", result.status() + "->single(" + data + ")");
        }
    }
}
