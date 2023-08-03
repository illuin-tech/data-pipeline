package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalInitializerIllegalArgumentObject<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalInitializerIllegalArgumentObject.class);

    @InitializerConfig
    public TestPayload execute(@Object T data, UIDGenerator generator)
    {
        logger.info("object: {}", data);
        return new TestPayload(new TestObject(generator.generate(), "illegal_object"));
    }
}
