package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalInitializerVoidMethod<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalInitializerVoidMethod.class);

    @InitializerConfig
    public void execute(@Input T data)
    {
        logger.info("input: {}", data);
    }
}
