package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndLatest<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndLatest.class);

    public SinkWithInputAndLatest(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input+latest")
    public void execute(@Input T data, @Latest TestResult result)
    {
        logger.info("input: {} latest: {}", data, result.status());
        this.collector.update(result.status() + "->single(" + data + ")");
    }

    public static class Required<T>
    {
        private final StringCollector collector;

        public Required(StringCollector collector)
        {
            this.collector = collector;
        }

        @SinkConfig(id = "sink-with_input+latest")
        public void execute(@Input T data, @Latest(required = true) TestResult result)
        {
            logger.info("input: {} latest: {}", data, result.status());
            this.collector.update(result.status() + "->single(" + data + ")");
        }
    }

    public static class Named<T>
    {
        private final StringCollector collector;

        public Named(StringCollector collector)
        {
            this.collector = collector;
        }

        @SinkConfig(id = "sink-with_input+latest")
        public void execute(@Input T data, @Latest(name = "annotation-named") TestResult result)
        {
            logger.info("input: {} latest: {}", data, result.status());
            this.collector.update(result.status() + "->single(" + data + ")");
        }
    }
}
