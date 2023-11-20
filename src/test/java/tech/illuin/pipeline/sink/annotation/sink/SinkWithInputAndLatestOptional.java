package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndLatestOptional<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndLatestOptional.class);

    public SinkWithInputAndLatestOptional(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input+latest-optional")
    public void execute(@Input T data, @Latest Optional<TestResult> result)
    {
        String status = result.map(TestResult::status).orElse(null);
        logger.info("input: {} latest: {}", data, status);
        this.collector.update(status + "->" + data);
    }

    public static class Named<T>
    {
        private final StringCollector collector;

        public Named(StringCollector collector)
        {
            this.collector = collector;
        }

        @SinkConfig(id = "sink-with_input+latest-optional")
        public void execute(@Input T data, @Latest(name = "annotation-named") Optional<TestResult> result)
        {
            String status = result.map(TestResult::status).orElse(null);
            logger.info("input: {} latest: {}", data, status);
            this.collector.update(status + "->optional(" + data + ")");
        }
    }
}
