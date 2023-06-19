package tech.illuin.pipeline.sink;

import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Sink<T>
{
    void execute(Output<T> output, Context<T> context) throws Exception;

    default void execute(Output<T> output) throws Exception
    {
        this.execute(output, output.context());
    }

    default String defaultId()
    {
        return Reflection.isAnonymousImplementation(this.getClass()) ? "anonymous-sink" : this.getClass().getName();
    }
}
