package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface OutputFactory<I, P>
{
    Output<P> create(String uid, String pipelineId, String author, I input, Context<P> context);
}
