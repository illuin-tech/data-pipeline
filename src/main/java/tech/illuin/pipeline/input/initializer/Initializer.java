package tech.illuin.pipeline.input.initializer;


import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface Initializer<I, P>
{
    P initialize(I input, Context<P> context);

    static <I, P> P initializeFromParent(I input, Context<P> context)
    {
        return context.parent().map(Output::payload).orElseThrow(() -> new IllegalArgumentException("The provided context does not reference a parent output"));
    }
}
