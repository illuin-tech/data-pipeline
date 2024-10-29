package tech.illuin.pipeline.sink.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static tech.illuin.pipeline.execution.error.PipelineErrorHandler.throwUnlessExcepted;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface SinkErrorHandler
{
    void handle(Exception exception, Output output, Context context) throws Exception;

    SinkErrorHandler RETHROW_ALL = (Exception ex, Output output, Context ctx) -> {
        throw ex;
    };

    @SuppressWarnings("IllegalCatch")
    default SinkErrorHandler andThen(SinkErrorHandler nextErrorHandler)
    {
        return (Exception exception, Output output, Context context) -> {
            try {
                handle(exception, output, context);
            }
            catch (Exception e) {
                nextErrorHandler.handle(e, output, context);
            }
        };
    }

    @SafeVarargs
    static SinkErrorHandler rethrowAllExcept(Class<? extends Throwable>... except)
    {
        return rethrowAllExcept(Arrays.asList(except));
    }

    static SinkErrorHandler rethrowAllExcept(Collection<Class<? extends Throwable>> except)
    {
        return (ex, out, ctx) -> throwUnlessExcepted(ex, except);
    }
}
