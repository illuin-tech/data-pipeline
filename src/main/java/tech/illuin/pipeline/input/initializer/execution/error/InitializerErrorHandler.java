package tech.illuin.pipeline.input.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface InitializerErrorHandler<P>
{
    P handle(Exception exception, Context<P> context, UIDGenerator generator) throws Exception;

    InitializerErrorHandler<?> RETHROW_ALL = (ex, ctx, gen) -> {
        throw ex;
    };
}
