package tech.illuin.pipeline.step.execution.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterStep<T extends Indexable, I, P> implements Step<T, I, P>
{
    private final Step<T, I, P> step;
    private final TimeLimiter limiter;
    private final Executor executor;

    public TimeLimiterStep(Step<T, I, P> step, TimeLimiter limiter, Executor executor)
    {
        this.step = step;
        this.limiter = limiter;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, P payload, ResultView view, Context<P> context) throws StepException
    {
        try {
            return this.limiter.executeFutureSupplier(() -> this.executeAsFuture(object, input, payload, view, context));
        }
        catch (TimeLimiterStepException e) {
            throw e.getCause();
        }
        /* If a timeout occurs, we throw a specific kind of runtime exception */
        catch (Exception e) {
            throw new TimeLimiterException(e.getMessage(), e);
        }
    }

    public Future<Result> executeAsFuture(T object, I input, P payload, ResultView view, Context<P> context)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.step.execute(object, input, payload, view, context);
            }
            catch (StepException e) {
                throw new TimeLimiterStepException(e.getMessage(), e);
            }
        }, this.executor);
    }

    @Override
    public String defaultId()
    {
        return "time-limiter." + this.step.defaultId();
    }
}
