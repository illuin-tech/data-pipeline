package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndCurrentOptional<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndCurrentOptional.class);

    public SinkWithInputAndCurrentOptional(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input+current-optional")
    public void execute(@Input T data, @Current Optional<TestResult> result)
    {
        String status = result.map(TestResult::status).orElse(null);
        logger.info("input: {} current: {}", data, status);
        this.collector.update(status + "->" + data);
    }
}
