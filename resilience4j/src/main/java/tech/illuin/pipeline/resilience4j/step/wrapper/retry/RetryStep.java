package tech.illuin.pipeline.resilience4j.step.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryException;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetryStepHandler;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapperException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.Map;

import static tech.illuin.pipeline.metering.MeterRegistryKey.fill;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryStep<T extends Indexable, I> implements Step<T, I>
{
    private final Step<T, I> step;
    private final Retry retry;
    private final RetryStepHandler handler;

    public static final String RUN_COUNT_KEY = "pipeline.step.retry.run_count";
    public static final String RETRY_COUNT_KEY = "pipeline.step.retry.retry_count";
    public static final String RUN_SUCCESS_KEY = "pipeline.step.retry.run_success";
    public static final String RETRY_SUCCESS_KEY = "pipeline.step.retry.retry_success";
    public static final String RUN_FAILURE_KEY = "pipeline.step.retry.run_failure";
    public static final String RETRY_FAILURE_KEY = "pipeline.step.retry.retry_failure";

    private static final Logger logger = LoggerFactory.getLogger(RetryStep.class);

    public RetryStep(Step<T, I> step, Retry retry, RetryStepHandler handler)
    {
        this.step = step;
        this.retry = retry;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            counter(RUN_COUNT_KEY, context).increment();

            Result result = this.retry.executeCallable(() -> {
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
            throw new RetryException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result executeStep(T object, I input, Object payload, ResultView view, LocalContext context) throws StepWrapperException
    {
        try {
            this.onAttempt(object, input, payload, view, context);
            Result result = this.step.execute(object, input, payload, view, context);

            counter(RETRY_SUCCESS_KEY, context).increment();
            return result;
        }
        catch (Exception e) {
            counter(RETRY_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw new StepWrapperException(e);
        }
    }

    private static Counter counter(String key, LocalContext context, Tag... tags)
    {
        MeterRegistry registry = context.observabilityManager().meterRegistry();
        return registry.counter(key, fill(key, context.markerManager().tags(tags)));
    }

    private void onSuccess(T object, I input, Object payload, ResultView view, LocalContext context)
    {
        try {
            logger.trace(
                "{}#{} retry wrapper {} succeeded - attempt count: {}",
                context.pipelineTag().pipeline(),
                context.pipelineTag().uid(),
                context.componentTag().id(),
                this.retry.getMetrics().getNumberOfTotalCalls()
            );
            this.handler.onSuccess(object, input, payload, view, context);
        }
        finally {
            counter(RUN_SUCCESS_KEY, context).increment();
        }
    }

    private void onError(T object, I input, Object payload, ResultView view, LocalContext context, Exception ex)
    {
        try {
            logger.trace(
                "{}#{} retry wrapper {} threw an {}: {} - max retry attempts: {}",
                context.pipelineTag().pipeline(),
                context.pipelineTag().uid(),
                context.componentTag().id(),
                ex.getClass().getName(),
                ex.getMessage(),
                this.retry.getRetryConfig().getMaxAttempts()
            );
            this.handler.onError(object, input, payload, view, context, ex);
        }
        finally {
            counter(RUN_FAILURE_KEY, context, Tag.of("error", ex.getClass().getName())).increment();
        }
    }

    private void onAttempt(T object, I input, Object payload, ResultView view, LocalContext context)
    {
        try {
            long totalCalls = this.retry.getMetrics().getNumberOfTotalCalls();
            logger.trace(
                "{}#{} retry wrapper {} - retry attempt #{} - {} left",
                context.pipelineTag().pipeline(),
                context.pipelineTag().uid(),
                context.componentTag().id(),
                totalCalls,
                this.retry.getRetryConfig().getMaxAttempts() - (totalCalls + 1)
            );
            this.handler.onRetry(object, input, payload, view, context);
        }
        finally {
            counter(RETRY_COUNT_KEY, context).increment();
        }
    }

    @Override
    public String defaultId()
    {
        return "retry." + this.step.defaultId();
    }
}
