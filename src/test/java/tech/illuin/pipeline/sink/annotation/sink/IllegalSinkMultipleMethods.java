package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalSinkMultipleMethods<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalSinkMultipleMethods.class);

    @SinkConfig
    public void execute(@Input T data)
    {
        logger.info("input: {}", data);
    }

    @SinkConfig
    public void anotherExecute(@Input T data)
    {
        logger.info("input: {}", data);
    }
}
