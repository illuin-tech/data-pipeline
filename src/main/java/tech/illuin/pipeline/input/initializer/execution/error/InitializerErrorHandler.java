package tech.illuin.pipeline.input.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface InitializerErrorHandler
{
    Indexable handle(Exception exception, Context context, UIDGenerator generator) throws Exception;

    InitializerErrorHandler RETHROW_ALL = (ex, ctx, gen) -> {
        throw ex;
    };
}
