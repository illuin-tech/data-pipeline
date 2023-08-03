package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithTags
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithTags.class);

    @StepConfig(id = "step-with_tags")
    public Result execute(PipelineTag pipelineTag, ComponentTag componentTag)
    {
        logger.info("pipeline_tag: {} component_tag: {}", pipelineTag, componentTag);
        return new TestResult("annotation-test", pipelineTag.pipeline()+"#"+componentTag.id());
    }
}
