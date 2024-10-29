package tech.illuin.pipeline.input.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;

import java.util.Arrays;
import java.util.Collection;

import static tech.illuin.pipeline.execution.error.PipelineErrorHandler.throwUnlessExcepted;

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

    @SafeVarargs
    static InitializerErrorHandler rethrowAllExcept(InitializerErrorHandler wrapper, Class<? extends Throwable>... except)
    {
        return rethrowAllExcept(wrapper, Arrays.asList(except));
    }

    static InitializerErrorHandler rethrowAllExcept(InitializerErrorHandler wrapper, Collection<Class<? extends Throwable>> except)
    {
        return (ex, ctx, gen) -> {
            throwUnlessExcepted(ex, except);
            return wrapper.handle(ex, ctx, gen);
        };
    }
}
