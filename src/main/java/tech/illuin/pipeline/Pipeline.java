package tech.illuin.pipeline;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface Pipeline<I, P>
{
    default String id()
    {
        return this.getClass().getName();
    }

    Output<P> run(I input, Context<P> context) throws PipelineException;

    default Output<P> run() throws PipelineException
    {
        return this.run(null);
    }

    default Output<P> run(I input) throws PipelineException
    {
        return this.run(input, null);
    }
}
