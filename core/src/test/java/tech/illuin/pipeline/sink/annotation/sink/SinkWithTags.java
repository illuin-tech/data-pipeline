package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import static tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithTags
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithTags.class);

    public SinkWithTags(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_tags")
    public void execute(PipelineTag pipelineTag, ComponentTag componentTag)
    {
        logger.info("pipeline_tag: {} component_tag: {}", pipelineTag, componentTag);
        this.collector.update(pipelineTag.pipeline()+"#"+componentTag.id());
    }
}
