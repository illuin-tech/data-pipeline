package tech.illuin.pipeline.step.execution.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryStep<T extends Indexable, I, P> implements Step<T, I, P>
{
    private final Step<T, I, P> step;
    private final Retry retry;

    public RetryStep(Step<T, I, P> step, Retry retry)
    {
        this.step = step;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, P payload, ResultView view, Context<P> context) throws StepException
    {
        try {
            return this.retry.executeCallable(() -> this.step.execute(object, input, payload, view, context));
        }
        catch (StepException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String defaultId()
    {
        return "retry." + this.step.defaultId();
    }
}
