package tech.illuin.pipeline.resilience4j.step.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterException;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapperException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterStep<T extends Indexable, I> implements Step<T, I>
{
    private final Step<T, I> step;
    private final TimeLimiter limiter;
    private final ExecutorService executor;

    public TimeLimiterStep(Step<T, I> step, TimeLimiter limiter, ExecutorService executor)
    {
        this.step = step;
        this.limiter = limiter;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            return this.limiter.executeFutureSupplier(() -> this.executor.submit(() -> {
                MDC.setContextMap(mdc);
                return executeStep(object, input, payload, view, context);
            }));
        }
        catch (StepWrapperException e) {
            throw (Exception) e.getCause();
        }
        /* If a timeout occurs, we throw a specific kind of runtime exception */
        catch (Exception e) {
            throw new TimeLimiterException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result executeStep(T object, I input, Object payload, ResultView view, LocalContext context) throws StepWrapperException
    {
        try {
            return this.step.execute(object, input, payload, view, context);
        }
        catch (Exception e) {
            throw new StepWrapperException(e);
        }
    }

    @Override
    public String defaultId()
    {
        return "time-limiter." + this.step.defaultId();
    }
}
