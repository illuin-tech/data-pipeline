package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DefaultOutputFactory<I> implements OutputFactory<I>
{
    @Override
    public Output create(PipelineTag tag, I input, Object payload, Context context)
    {
        return new Output(tag, payload, context);
    }
}
