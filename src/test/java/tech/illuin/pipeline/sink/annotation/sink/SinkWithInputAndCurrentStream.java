package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndCurrentStream<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndCurrentStream.class);

    public SinkWithInputAndCurrentStream(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input+current-stream")
    public void execute(@Input T data, @Current Stream<TestResult> result)
    {
        String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
        logger.info("input: {} currents: {}", data, statuses);
        this.collector.update(statuses + "->stream(" + data + ")");
    }

    public static class Named<T>
    {
        private final StringCollector collector;

        public Named(StringCollector collector)
        {
            this.collector = collector;
        }

        @SinkConfig(id = "sink-with_input+current-stream")
        public void execute(@Input T data, @Current(name = "annotation-named") Stream<TestResult> result)
        {
            String statuses = result.map(TestResult::status).collect(Collectors.joining("+"));
            logger.info("input: {} currents: {}", data, statuses);
            this.collector.update(statuses + "->stream(" + data + ")");
        }
    }
}
