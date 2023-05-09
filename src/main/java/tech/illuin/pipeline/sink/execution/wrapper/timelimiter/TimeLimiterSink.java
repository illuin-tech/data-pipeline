package tech.illuin.pipeline.sink.execution.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterSink<T extends Indexable, I, P> implements Sink<P>
{
    private final Sink<P> sink;
    private final TimeLimiter limiter;
    private final Executor executor;

    public TimeLimiterSink(Sink<P> sink, TimeLimiter limiter, Executor executor)
    {
        this.sink = sink;
        this.limiter = limiter;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output<P> output, Context<P> context) throws SinkException
    {
        try {
            this.limiter.executeFutureSupplier(() -> this.executeAsFuture(output, context));
        }
        catch (SinkException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Future<Void> executeAsFuture(Output<P> output, Context<P> context)
    {
        return CompletableFuture.runAsync(() -> {
            try {
                this.sink.execute(output, context);
            }
            catch (SinkException e) {
                throw new RuntimeException(e);
            }
        }, this.executor);
    }

    @Override
    public String defaultId()
    {
        return "time-limiter." + this.sink.defaultId();
    }
}
