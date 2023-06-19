package tech.illuin.pipeline.step.execution.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapperException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerStep<T extends Indexable, I, P> implements Step<T, I, P>
{
    private final Step<T, I, P> step;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerStep(Step<T, I, P> step, CircuitBreaker circuitBreaker)
    {
        this.step = step;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, P payload, ResultView view, Context<P> context) throws Exception
    {
        try {
            return this.circuitBreaker.executeCallable(() -> executeStep(object, input, payload, view, context));
        }
        catch (StepWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new CircuitBreakerException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result executeStep(T object, I input, P payload, ResultView view, Context<P> context) throws StepWrapperException
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
        return "circuit-breaker." + this.step.defaultId();
    }
}
