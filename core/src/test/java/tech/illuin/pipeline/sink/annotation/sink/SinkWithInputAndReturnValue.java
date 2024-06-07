package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithInputAndReturnValue<T>
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithInputAndReturnValue.class);

    public SinkWithInputAndReturnValue(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_input_return-value")
    public String execute(@Input T data)
    {
        logger.info("input: {}", data);
        return this.collector.update(Objects.toString(data));
    }
}
