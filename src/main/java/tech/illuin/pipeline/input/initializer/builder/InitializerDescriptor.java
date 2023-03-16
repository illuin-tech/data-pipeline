package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.InitializerException;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;

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

    public P execute(I input, Context<P> context) throws InitializerException
    {
        return this.initializer.initialize(input, context);
    }

    public P handleException(Exception ex, Context<P> ctx) throws InitializerException
    {
        return this.errorHandler.handle(ex, ctx);
    }

    public String id()
    {
        return id;
    }
}
