package tech.illuin.pipeline.resilience4j.step.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.resilience4j.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapperException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerStep<T extends Indexable, I> implements Step<T, I>
{
    private final Step<T, I> step;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerStep(Step<T, I> step, CircuitBreaker circuitBreaker)
    {
        this.step = step;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, Context context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            return this.circuitBreaker.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeStep(object, input, payload, view, context);
            });
        }
        catch (StepWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new CircuitBreakerException(e.getMessage(), e);
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
        return "circuit-breaker." + this.step.defaultId();
    }
}
