package tech.illuin.pipeline.context;

import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface LocalContext<P> extends Context<P>
{
    Object input();

    PipelineTag pipelineTag();

    ComponentTag componentTag();

    UIDGenerator uidGenerator();
}
