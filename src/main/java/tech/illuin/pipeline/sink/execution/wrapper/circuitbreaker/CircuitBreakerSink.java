package tech.illuin.pipeline.sink.execution.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.wrapper.CircuitBreakerException;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerSink<P> implements Sink<P>
{
    private final Sink<P> sink;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerSink(Sink<P> sink, CircuitBreaker circuitBreaker)
    {
        this.sink = sink;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output<P> output, Context<P> context) throws Exception
    {
        try {
            this.circuitBreaker.executeCallable(() -> executeSink(output, context));
        }
        catch (SinkWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new CircuitBreakerException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private boolean executeSink(Output<P> output, Context<P> context) throws SinkWrapperException
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
