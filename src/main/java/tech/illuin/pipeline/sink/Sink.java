package tech.illuin.pipeline.sink;

import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.runner.SinkRunner;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Sink<T>
{
    void execute(Output<T> output, Context<T> context) throws Exception;

    default String defaultId()
    {
        return Reflection.isAnonymousImplementation(this.getClass()) ? "anonymous-sink" : this.getClass().getName();
    }

    /**
     * Create a {@link Sink} out of a non-specific instance (i.e. not a {@link Sink} implementation).
     * The resulting sink will leverage reflection-based method introspection mechanisms for resolving execution configuration.
     */
    static <T> Sink<T> of(Object target)
    {
        return new SinkRunner<>(target);
    }
}
