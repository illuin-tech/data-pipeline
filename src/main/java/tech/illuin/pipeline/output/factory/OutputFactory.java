package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface OutputFactory<I>
{
    Output create(PipelineTag tag, I input, Object payload, Context context);
}
