package tech.illuin.pipeline.resilience4j.step.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.circuitbreaker.CircuitBreakerStepHandler;
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
    private final CircuitBreakerStepHandler handler;

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerStep.class);

    public CircuitBreakerStep(Step<T, I> step, CircuitBreaker circuitBreaker, CircuitBreakerStepHandler handler)
    {
        this.step = step;
        this.circuitBreaker = circuitBreaker;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            Result result = this.circuitBreaker.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeStep(object, input, payload, view, context);
            });

            this.onSuccess(object, input, payload, view, context);
            return result;
        }
        catch (StepWrapperException e) {
            this.onError(object, input, payload, view, context, e);
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            this.onError(object, input, payload, view, context, e);
            throw new CircuitBreakerException(e.getMessage(), e);
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

    private void onSuccess(T object, I input, Object payload, ResultView view, LocalContext context)
    {
        logger.trace(
            "{}#{} circuit-breaker wrapper {} succeeded",
            context.pipelineTag().pipeline(),
            context.pipelineTag().uid(),
            context.componentTag().id()
        );
        this.handler.onSuccess(object, input, payload, view, context);
    }

    private void onError(T object, I input, Object payload, ResultView view, LocalContext context, Exception ex)
    {
        logger.debug(
            "{}#{} circuit-breaker wrapper {} threw an {}: {}",
            context.pipelineTag().pipeline(),
            context.pipelineTag().uid(),
            context.componentTag().id(),
            ex.getClass().getName(),
            ex.getMessage()
        );
        this.handler.onError(object, input, payload, view, context, ex);
    }

    @Override
    public String defaultId()
    {
        return "circuit-breaker." + this.step.defaultId();
    }
}
