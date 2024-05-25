package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithInput<T>
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithInput.class);

    @InitializerConfig(id = "init-with_input")
    public TestPayload execute(@Input T data, UIDGenerator generator)
    {
        logger.info("input: {}", data);
        return new TestPayload(new TestObject(generator.generate(), Objects.toString(data)));
    }
}
