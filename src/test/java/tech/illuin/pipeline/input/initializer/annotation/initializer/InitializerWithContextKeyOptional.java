package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithContextKeyOptional
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithContextKeyOptional.class);

    @InitializerConfig(id = "init-with_context-key-optional")
    public TestPayload execute(@Context("some-metadata") Optional<String> metadata, UIDGenerator generator)
    {
        String value = metadata.orElse(null);
        logger.info("context-key: {}", value);
        return new TestPayload(new TestObject(generator.generate(), "optional(" + value + ")"));
    }
}
