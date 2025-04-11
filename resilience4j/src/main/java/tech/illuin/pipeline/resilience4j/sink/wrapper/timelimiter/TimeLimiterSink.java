package tech.illuin.pipeline.resilience4j.sink.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterException;
import tech.illuin.pipeline.sink.Sink;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterSink implements Sink
{
    private final Sink sink;
    private final TimeLimiter limiter;
    private final ExecutorService executor;

    public TimeLimiterSink(Sink sink, TimeLimiter limiter, ExecutorService executor)
    {
        this.sink = sink;
        this.limiter = limiter;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            this.limiter.executeFutureSupplier(() -> this.executor.submit(() -> {
                MDC.setContextMap(mdc);
                this.executeSink(output, context);
            }));
        }
        catch (TimeLimiterSinkException e) {
            throw e.getCause();
        }
        /* If a timeout occurs, we throw a specific kind of runtime exception */
        catch (Exception e) {
            throw new TimeLimiterException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void executeSink(Output output, LocalContext context) throws TimeLimiterSinkException
    {
        try {
            this.sink.execute(output, context);
        }
        catch (Exception e) {
            throw new TimeLimiterSinkException(e);
        }
    }

    @Override
    public String defaultId()
    {
        return "time-limiter." + this.sink.defaultId();
    }
}
