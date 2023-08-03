package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithException<T>
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithException.class);

    @InitializerConfig(id = "init-with_input+exception")
    public TestPayload execute(@Input T data) throws InitializerWithExceptionException
    {
        logger.info("input: {}", data);
        throw new InitializerWithExceptionException("This is an exception for input " + data);
    }

    public static class InitializerWithExceptionException extends Exception
    {
        public InitializerWithExceptionException(String message)
        {
            super(message);
        }
    }
}
