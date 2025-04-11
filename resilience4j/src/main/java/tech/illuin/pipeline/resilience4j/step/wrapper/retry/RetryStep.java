package tech.illuin.pipeline.resilience4j.step.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryException;
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

    public static final String RUN_COUNT_KEY = "pipeline.step.retry.run_count";
    public static final String RETRY_COUNT_KEY = "pipeline.step.retry.retry_count";
    public static final String RUN_SUCCESS_KEY = "pipeline.step.retry.run_success";
    public static final String RETRY_SUCCESS_KEY = "pipeline.step.retry.retry_success";
    public static final String RUN_FAILURE_KEY = "pipeline.step.retry.run_failure";
    public static final String RETRY_FAILURE_KEY = "pipeline.step.retry.retry_failure";

    public RetryStep(Step<T, I> step, Retry retry)
    {
        this.step = step;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Result execute(T object, I input, Object payload, ResultView view, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            Result result = this.retry.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeStep(object, input, payload, view, context);
            });

            counter(RUN_SUCCESS_KEY, context).increment();
            return result;
        }
        catch (StepWrapperException e) {
            counter(RUN_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            counter(RUN_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw new RetryException(e.getMessage(), e);
        }
        finally {
            counter(RUN_COUNT_KEY, context).increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result executeStep(T object, I input, Object payload, ResultView view, LocalContext context) throws StepWrapperException
    {
        try {
            Result result = this.step.execute(object, input, payload, view, context);
            counter(RETRY_SUCCESS_KEY, context).increment();
            return result;
        }
        catch (Exception e) {
            counter(RETRY_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw new StepWrapperException(e);
        }
        finally {
            counter(RETRY_COUNT_KEY, context).increment();
        }
    }

    private static Counter counter(String key, LocalContext context, Tag... tags)
    {
        MeterRegistry registry = context.observabilityManager().meterRegistry();
        return registry.counter(key, fill(key, context.markerManager().tags(tags)));
    }

    @Override
    public String defaultId()
    {
        return "retry." + this.step.defaultId();
    }
}
