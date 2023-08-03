package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithException<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SinkWithException.class);

    @SinkConfig(id = "sink-with_input+exception")
    public void execute(@Input T data) throws SinkWithExceptionException
    {
        logger.info("input: {}", data);
        throw new SinkWithExceptionException("This is an exception for input " + data);
    }

    public static class SinkWithExceptionException extends Exception
    {
        public SinkWithExceptionException(String message)
        {
            super(message);
        }
    }
}
