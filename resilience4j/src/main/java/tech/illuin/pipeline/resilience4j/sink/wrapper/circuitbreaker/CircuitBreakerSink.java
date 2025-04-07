package tech.illuin.pipeline.resilience4j.sink.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.circuitbreaker.CircuitBreakerSinkHandler;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerSink implements Sink
{
    private final Sink sink;
    private final CircuitBreaker circuitBreaker;
    private final CircuitBreakerSinkHandler handler;

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerSink.class);

    public CircuitBreakerSink(Sink sink, CircuitBreaker circuitBreaker, CircuitBreakerSinkHandler handler)
    {
        this.sink = sink;
        this.circuitBreaker = circuitBreaker;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, LocalContext context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            this.circuitBreaker.executeCallable(() -> {
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
            throw new CircuitBreakerException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private boolean executeSink(Output output, LocalContext context) throws SinkWrapperException
    {
        try {
            this.sink.execute(output, context);
            return true;
        }
        catch (Exception e) {
            throw new SinkWrapperException(e);
        }
    }

    private void onSuccess(Output output, LocalContext context)
    {
        logger.trace(
            "{}#{} circuit-breaker wrapper {} succeeded",
            context.pipelineTag().pipeline(),
            context.pipelineTag().uid(),
            context.componentTag().id()
        );
        this.handler.onSuccess(output, context);
    }

    private void onError(Output output, LocalContext context, Exception ex)
    {
        logger.debug(
            "{}#{} circuit-breaker wrapper {} threw an {}: {}",
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
        return "circuit-breaker." + this.sink.defaultId();
    }
}
