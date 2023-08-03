package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithContext
{
    private final String key;

    private static final Logger logger = LoggerFactory.getLogger(InitializerWithContext.class);

    public InitializerWithContext(String key)
    {
        this.key = key;
    }

    @InitializerConfig(id = "init-with_context")
    public TestPayload execute(Context<?> context, UIDGenerator generator)
    {
        logger.info("context: {}", context);
        return new TestPayload(new TestObject(generator.generate(), context.get(this.key, String.class).orElse(null)));
    }
}
