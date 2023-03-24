package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DefaultOutputFactory<I, P> implements OutputFactory<I, P>
{
    @Override
    public Output<P> create(PipelineTag tag, I input, Context<P> context)
    {
        return new Output<>(tag);
    }
}
