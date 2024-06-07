package tech.illuin.pipeline.context;

import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface LocalContext extends Context
{
    Object input();

    PipelineTag pipelineTag();

    ComponentTag componentTag();

    UIDGenerator uidGenerator();

    LogMarker logMarker();
}
