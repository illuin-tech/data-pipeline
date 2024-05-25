package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithTags
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithTags.class);

    @InitializerConfig(id = "init-with_tags")
    public TestPayload execute(PipelineTag pipelineTag, ComponentTag componentTag, UIDGenerator generator)
    {
        logger.info("pipeline_tag: {} component_tag: {}", pipelineTag, componentTag);
        return new TestPayload(new TestObject(generator.generate(), pipelineTag.pipeline()+"#"+componentTag.id()));
    }
}
