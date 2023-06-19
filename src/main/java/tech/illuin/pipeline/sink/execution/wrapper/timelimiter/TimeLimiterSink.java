package tech.illuin.pipeline.sink.execution.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.wrapper.TimeLimiterException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;

import java.util.concurrent.ExecutorService;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterSink<T extends Indexable, I, P> implements Sink<P>
{
    private final Sink<P> sink;
    private final TimeLimiter limiter;
    private final ExecutorService executor;

    public TimeLimiterSink(Sink<P> sink, TimeLimiter limiter, ExecutorService executor)
    {
        this.sink = sink;
        this.limiter = limiter;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output<P> output, Context<P> context) throws Exception
    {
        try {
            this.limiter.executeFutureSupplier(() -> this.executor.submit(() -> this.executeSink(output, context)));
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
    private void executeSink(Output<P> output, Context<P> context) throws TimeLimiterSinkException
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
