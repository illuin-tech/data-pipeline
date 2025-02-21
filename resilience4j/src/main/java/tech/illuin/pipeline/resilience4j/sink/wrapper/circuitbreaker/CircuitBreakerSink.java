package tech.illuin.pipeline.resilience4j.sink.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.resilience4j.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.output.Output;
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

    public CircuitBreakerSink(Sink sink, CircuitBreaker circuitBreaker)
    {
        this.sink = sink;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, Context context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            this.circuitBreaker.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeSink(output, context);
            });
        }
        catch (SinkWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new CircuitBreakerException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private boolean executeSink(Output output, Context context) throws SinkWrapperException
    {
        try {
            this.sink.execute(output, context);
            return true;
        }
        catch (Exception e) {
            throw new SinkWrapperException(e);
        }
    }

    @Override
    public String defaultId()
    {
        return "circuit-breaker." + this.sink.defaultId();
    }
}
