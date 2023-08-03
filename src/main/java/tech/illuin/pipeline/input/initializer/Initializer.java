package tech.illuin.pipeline.input.initializer;


import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.runner.InitializerRunner;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.Output;

import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface Initializer<I, P>
{
    P initialize(I input, Context<P> context, UIDGenerator generator) throws Exception;

    static <I, P> P initializeFromParent(I input, Context<P> context)
    {
        return context.parent().map(Output::payload).orElseThrow(() -> new IllegalArgumentException("The provided context does not reference a parent output"));
    }

    static <I, P> P initializeFromParentOr(Context<P> context, Supplier<P> or)
    {
        return context.parent().map(Output::payload).orElseGet(or);
    }

    default String defaultId()
    {
        return Reflection.isAnonymousImplementation(this.getClass()) ? "anonymous-init" : this.getClass().getName();
    }

    /**
     * Create an {@link Initializer} out of a non-specific instance (i.e. not an {@link Initializer} implementation).
     * The resulting initializer will leverage reflection-based method introspection mechanisms for resolving execution configuration.
     */
    static <I, P> Initializer<I, P> of(Object target)
    {
        return new InitializerRunner<>(target);
    }
}
