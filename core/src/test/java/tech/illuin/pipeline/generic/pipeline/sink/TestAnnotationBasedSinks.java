package tech.illuin.pipeline.generic.pipeline.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestAnnotationBasedSinks
{
    private static final Logger logger = LoggerFactory.getLogger(TestAnnotationBasedSinks.class);

    public static class InputSink<T>
    {
        @SinkConfig
        public void execute(@Input T data)
        {
            logger.info("test: {}", data);
        }
    }

    public static class InputSinkWithCurrent<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Current TestResult result)
        {
            logger.info("test: {} current: {}", data, result.status());
        }
    }

    public static class InputSinkWithCurrentOptional<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Current Optional<TestResult> result)
        {
            String status = result.map(TestResult::status).orElse(null);
            logger.info("test: {} current: {}", data, status);
        }
    }

    public static class InputSinkWithCurrentStream<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Current Stream<TestResult> result)
        {
            String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
            logger.info("test: {} currents: {}", data, statuses);
        }
    }

    public static class InputSinkWithLatest<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Latest TestResult result)
        {
            logger.info("test: {} latest: {}", data, result.status());
        }
    }

    public static class InputSinkWithLatestOptional<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Latest Optional<TestResult> result)
        {
            String status = result.map(TestResult::status).orElse(null);
            logger.info("test: {} latest: {}", data, status);
        }
    }

    public static class InputSinkWithLatestStream<T>
    {
        @SinkConfig
        public void execute(@Input T data, @Latest Stream<TestResult> result)
        {
            String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
            logger.info("test: {} latests: {}", data, statuses);
        }
    }

    public static class IllegalSinkNoMethod
    {
        public void whatever() {}
    }

    public static class IllegalSinkMultipleMethods<T>
    {
        @SinkConfig
        public void execute(@Input T data)
        {
            logger.info("test: {}", data);
        }

        @SinkConfig
        public void anotherExecute(@Input T data)
        {
            logger.info("test: {}", data);
        }
    }

    public static class IllegalSinkMultipleAnnotations<T>
    {
        @SinkConfig
        public void execute(@Input @Object T data)
        {
            logger.info("test: {}", data);
        }
    }
}
