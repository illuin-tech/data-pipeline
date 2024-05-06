package tech.illuin.pipeline.step.execution.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.wrapper.RetryException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapperException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryStep<T extends Indexable, I> implements Step<T, I>
{
    private final Step<T, I> step;
    private final Retry retry;

    public RetryStep(Step<T, I> step, Retry retry)
    {
        this.step = step;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, Context context) throws Exception
    {
        try {
            return this.retry.executeCallable(() -> executeStep(object, input, payload, view, context));
        }
        catch (StepWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result executeStep(T object, I input, Object payload, ResultView view, Context context) throws StepWrapperException
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
        return "retry." + this.step.defaultId();
    }
}
