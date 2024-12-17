package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class InitializerDescriptor<I>
{
    private final String id;
    private final Initializer<I> initializer;
    private final InitializerErrorHandler errorHandler;

    InitializerDescriptor(
        String id,
        Initializer<I> initializer,
        InitializerErrorHandler errorHandler
    ) {
        this.id = id;
        this.initializer = initializer;
        this.errorHandler = errorHandler;
    }

    public Object execute(I input, Context ctx, UIDGenerator generator) throws Exception
    {
        return this.initializer.initialize(input, ctx, generator);
    }

    public Object handleException(Exception ex, Context ctx, UIDGenerator generator) throws Exception
    {
        return this.errorHandler.handle(ex, ctx, generator);
    }

    public String id()
    {
        return this.id;
    }

    public Initializer<I> initializer()
    {
        return this.initializer;
    }

    public InitializerErrorHandler errorHandler()
    {
        return this.errorHandler;
    }
}
