package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DefaultOutputFactory<I, P> implements OutputFactory<I, P>
{
    @Override
    public Output<P> create(String uid, String pipelineId, String author, I input, Context<P> context)
    {
        return new Output<>(uid, pipelineId, author);
    }
}
