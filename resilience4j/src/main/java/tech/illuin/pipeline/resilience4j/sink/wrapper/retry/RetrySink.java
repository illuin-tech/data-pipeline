package tech.illuin.pipeline.resilience4j.sink.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetrySinkHandler;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryException;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tech.illuin.pipeline.metering.MeterRegistryKey.fill;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetrySink implements Sink
{
    private final Sink sink;
    private final Retry retry;
    private final RetrySinkHandler handler;
    private final Map<String, Integer> retryCounterContainer;

    public static final String RUN_COUNT_KEY = "pipeline.sink.retry.run_count";
    public static final String RETRY_COUNT_KEY = "pipeline.sink.retry.retry_count";
    public static final String RUN_SUCCESS_KEY = "pipeline.sink.retry.run_success";
    public static final String RETRY_SUCCESS_KEY = "pipeline.sink.retry.retry_success";
    public static final String RUN_FAILURE_KEY = "pipeline.sink.retry.run_failure";
    public static final String RETRY_FAILURE_KEY = "pipeline.sink.retry.retry_failure";

    private static final Logger logger = LoggerFactory.getLogger(RetrySink.class);

    public RetrySink(Sink sink, Retry retry, RetrySinkHandler handler)
    {
        this.sink = sink;
        this.retry = retry;
        this.handler = handler;
        this.retryCounterContainer = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            counter(RUN_COUNT_KEY, context).increment();

            this.retry.executeCallable(() ->  {
                MDC.setContextMap(mdc);
                return executeSink(output, context);
            });

            this.onSuccess(output, context);
        }
        catch (SinkWrapperException e) {
            this.onError(output, context, e);
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            this.onError(output, context, e);
            throw new RetryException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private boolean executeSink(Output output, LocalContext context) throws SinkWrapperException
    {
        try {
            this.onAttempt(output, context);
            this.sink.execute(output, context);

            counter(RETRY_SUCCESS_KEY, context).increment();
            return true;
        }
        catch (Exception e) {
            counter(RETRY_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw new SinkWrapperException(e);
        }
    }

    private static Counter counter(String key, LocalContext context, Tag... tags)
    {
        MeterRegistry registry = context.observabilityManager().meterRegistry();
        return registry.counter(key, fill(key, context.markerManager().tags(tags)));
    }

    private void onSuccess(Output output, LocalContext context)
    {
        try {
            logger.trace(
                "{}#{} sink wrapper {} succeeded - attempt count: {}",
                context.pipelineTag().pipeline(),
                context.pipelineTag().uid(),
                context.componentTag().id(),
                this.retryCounterContainer.get(context.pipelineTag().uid())
            );
            this.handler.onSuccess(output, context);
        }
        finally {
            counter(RUN_SUCCESS_KEY, context).increment();
            this.retryCounterContainer.remove(context.pipelineTag().uid());
        }
    }

    private void onError(Output output, LocalContext context, Exception ex)
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
            this.handler.onError(output, context, ex);
        }
        finally {
            counter(RUN_FAILURE_KEY, context, Tag.of("error", ex.getClass().getName())).increment();
            this.retryCounterContainer.remove(context.pipelineTag().uid());
        }
    }

    private void onAttempt(Output output, LocalContext context)
    {
        try {
            if (!this.retryCounterContainer.containsKey(context.pipelineTag().uid()))
                this.retryCounterContainer.put(context.pipelineTag().uid(), 0);
            else {
                Integer retryCount = this.retryCounterContainer.merge(context.pipelineTag().uid(), 1, Integer::sum);
                logger.trace(
                    "{}#{} retry wrapper {} - retry attempt #{} - {} left",
                    context.pipelineTag().pipeline(),
                    context.pipelineTag().uid(),
                    context.componentTag().id(),
                    retryCount,
                    this.retry.getRetryConfig().getMaxAttempts() - retryCount
                );
            }
            this.handler.onRetry(output, context);
        }
        finally {
            counter(RETRY_COUNT_KEY, context).increment();
        }
    }

    @Override
    public String defaultId()
    {
        return "retry." + this.sink.defaultId();
    }
}
