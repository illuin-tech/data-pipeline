package tech.illuin.pipeline.output.factory;

import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DefaultOutputFactory<P> implements OutputFactory<P>
{
    @Override
    public Output<P> create(String pipelineId, String author)
    {
        return new Output<>(pipelineId, author);
    }
}
