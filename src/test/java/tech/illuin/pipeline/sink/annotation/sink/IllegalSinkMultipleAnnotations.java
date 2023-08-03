package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalSinkMultipleAnnotations<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalSinkMultipleAnnotations.class);

    @SinkConfig
    public void execute(@Input @Object T data)
    {
        logger.info("input-object: {}", data);
    }
}
