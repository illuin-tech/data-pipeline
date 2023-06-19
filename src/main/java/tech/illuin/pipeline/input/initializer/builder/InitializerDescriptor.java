package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class InitializerDescriptor<I, P>
{
    private final String id;
    private final Initializer<I, P> initializer;
    private final InitializerErrorHandler<P> errorHandler;

    InitializerDescriptor(
        String id,
        Initializer<I, P> initializer,
        InitializerErrorHandler<P> errorHandler
    ) {
        this.id = id;
        this.initializer = initializer;
        this.errorHandler = errorHandler;
    }

    public P execute(I input, Context<P> ctx, UIDGenerator generator) throws Exception
    {
        return this.initializer.initialize(input, ctx, generator);
    }

    public P handleException(Exception ex, Context<P> ctx, UIDGenerator generator) throws Exception
    {
        return this.errorHandler.handle(ex, ctx, generator);
    }

    public String id()
    {
        return id;
    }
}
