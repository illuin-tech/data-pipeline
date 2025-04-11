package tech.illuin.pipeline.step.execution.error;

import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

import java.util.Arrays;
import java.util.Collection;

import static tech.illuin.pipeline.execution.error.PipelineErrorHandler.throwUnlessExcepted;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface StepErrorHandler
{
    Result handle(Exception exception, Object input, Object payload, Results results, LocalContext context) throws Exception;

    StepErrorHandler RETHROW_ALL = (Exception ex, Object input, Object payload, Results results, LocalContext ctx) -> {
        throw ex;
    };

    @SuppressWarnings("IllegalCatch")
    default StepErrorHandler andThen(StepErrorHandler nextErrorHandler)
    {
        return (Exception exception, Object input, Object payload, Results results, LocalContext context) -> {
            try {
                return this.handle(exception, input, payload, results, context);
            }
            catch (Exception e) {
                return nextErrorHandler.handle(e, input, payload, results, context);
            }
        };
    }

    @SafeVarargs
    static StepErrorHandler rethrowAllExcept(StepErrorHandler wrapper, Class<? extends Throwable>... except)
    {
        return rethrowAllExcept(wrapper, Arrays.asList(except));
    }

    static StepErrorHandler rethrowAllExcept(StepErrorHandler wrapper, Collection<Class<? extends Throwable>> except)
    {
        return (ex, in, payload, res, ctx) -> {
            throwUnlessExcepted(ex, except);
            return wrapper.handle(ex, in, payload, res, ctx);
        };
    }
}
