package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithContextKey
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithContextKey.class);

    @InitializerConfig(id = "init-with_context-key")
    public TestPayload execute(@Context("some-metadata") String metadata, UIDGenerator generator)
    {
        logger.info("context-key: {}", metadata);
        return new TestPayload(new TestObject(generator.generate(), "single(" + metadata + ")"));
    }
}
