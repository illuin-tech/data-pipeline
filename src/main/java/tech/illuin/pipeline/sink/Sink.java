package tech.illuin.pipeline.sink;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Sink<T>
{
    void execute(Output<T> output, Context<T> context) throws SinkException;
}
