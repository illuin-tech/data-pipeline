package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithInputAndCurrentStream<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithInputAndCurrentStream.class);

    @StepConfig(id = "step-with_input+current-stream")
    public Result execute(@Input T data, @Current Stream<TestResult> result)
    {
        String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
        logger.info("input: {} currents: {}", data, statuses);
        return new TestResult("annotation-test", statuses + "->stream(" + data + ")");
    }

    public static class Named<T>
    {
        @StepConfig(id = "step-with_input+current-stream")
        public Result execute(@Input T data, @Current(name = "annotation-named") Stream<TestResult> result)
        {
            String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
            logger.info("input: {} currents: {}", data, statuses);
            return new TestResult("annotation-test", statuses + "->stream(" + data + ")");
        }
    }
}
