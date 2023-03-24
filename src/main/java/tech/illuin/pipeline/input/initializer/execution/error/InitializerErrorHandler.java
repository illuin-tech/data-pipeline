package tech.illuin.pipeline.input.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.initializer.InitializerException;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface InitializerErrorHandler<P>
{
    P handle(Exception exception, Context<P> context, UIDGenerator generator) throws InitializerException;

    InitializerErrorHandler<?> RETHROW_ALL = InitializerErrorHandler::rethrowAll;

    private static <P> P rethrowAll(Exception ex, Context<P> ctx, UIDGenerator generator) throws InitializerException
    {
        if (ex instanceof InitializerException initEx)
            throw initEx;
        if (ex instanceof RuntimeException runEx)
            throw runEx;
        throw new RuntimeException("Unexpected error type found in handler: " + ex.getClass().getName() + " with message " + ex.getMessage());
    }
}
