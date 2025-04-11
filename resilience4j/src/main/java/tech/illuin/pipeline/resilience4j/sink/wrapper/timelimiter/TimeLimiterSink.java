package tech.illuin.pipeline.resilience4j.sink.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterException;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterSinkHandler;
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
    private final TimeLimiterSinkHandler handler;

    private static final Logger logger = LoggerFactory.getLogger(TimeLimiterSink.class);

    public TimeLimiterSink(Sink sink, TimeLimiter limiter, ExecutorService executor, TimeLimiterSinkHandler handler)
    {
        this.sink = sink;
        this.limiter = limiter;
        this.executor = executor;
        this.handler = handler;
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

            this.onSuccess(output, context);
        }
        catch (TimeLimiterSinkException e) {
            this.onError(output, context, e);
            throw e.getCause();
        }
        /* If a timeout occurs, we throw a specific kind of runtime exception */
        catch (Exception e) {
            this.onError(output, context, e);
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

    private void onSuccess(Output output, LocalContext context)
    {
        logger.trace(
            "{}#{} time-limiter wrapper {} succeeded",
            context.pipelineTag().pipeline(),
            context.pipelineTag().uid(),
            context.componentTag().id()
        );
        this.handler.onSuccess(output, context);
    }

    private void onError(Output output, LocalContext context, Exception ex)
    {
        logger.debug(
            "{}#{} time-limiter wrapper {} threw an {}: {}",
            context.pipelineTag().pipeline(),
            context.pipelineTag().uid(),
            context.componentTag().id(),
            ex.getClass().getName(),
            ex.getMessage()
        );
        this.handler.onError(output, context, ex);
    }

    @Override
    public String defaultId()
    {
        return "time-limiter." + this.sink.defaultId();
    }
}
