package tech.illuin.pipeline.resilience4j.sink.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryException;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

import java.util.Map;

import static tech.illuin.pipeline.metering.MeterRegistryKey.fill;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetrySink implements Sink
{
    private final Sink sink;
    private final Retry retry;

    public static final String RUN_COUNT_KEY = "pipeline.sink.retry.run_count";
    public static final String RETRY_COUNT_KEY = "pipeline.sink.retry.retry_count";
    public static final String RUN_SUCCESS_KEY = "pipeline.sink.retry.run_success";
    public static final String RETRY_SUCCESS_KEY = "pipeline.sink.retry.retry_success";
    public static final String RUN_FAILURE_KEY = "pipeline.sink.retry.run_failure";
    public static final String RETRY_FAILURE_KEY = "pipeline.sink.retry.retry_failure";

    public RetrySink(Sink sink, Retry retry)
    {
        this.sink = sink;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            this.retry.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeSink(output, context);
            });

            counter(RUN_SUCCESS_KEY, context).increment();
        }
        catch (SinkWrapperException e) {
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
    private boolean executeSink(Output output, LocalContext context) throws SinkWrapperException
    {
        try {
            this.sink.execute(output, context);
            counter(RETRY_SUCCESS_KEY, context).increment();
            return true;
        }
        catch (Exception e) {
            counter(RETRY_FAILURE_KEY, context, Tag.of("error", e.getClass().getName())).increment();
            throw new SinkWrapperException(e);
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
        return "retry." + this.sink.defaultId();
    }
}
