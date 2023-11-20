package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndCurrent<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndCurrent.class);

    public SinkWithInputAndCurrent(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input+current")
    public void execute(@Input T data, @Current TestResult result)
    {
        logger.info("input: {} current: {}", data, result.status());
        this.collector.update(result.status() + "->" + data);
    }

    public static class Named<T>
    {
        private final StringCollector collector;

        public Named(StringCollector collector)
        {
            this.collector = collector;
        }

        @SinkConfig(id = "sink-with_input+current")
        public void execute(@Input T data, @Current(name = "annotation-named") TestResult result)
        {
            logger.info("input: {} current: {}", data, result.status());
            this.collector.update(result.status() + "->single(" + data + ")");
        }
    }
}
