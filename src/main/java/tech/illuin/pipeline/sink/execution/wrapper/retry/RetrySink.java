package tech.illuin.pipeline.sink.execution.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.wrapper.RetryException;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetrySink<P> implements Sink<P>
{
    private final Sink<P> sink;
    private final Retry retry;

    public RetrySink(Sink<P> sink, Retry retry)
    {
        this.sink = sink;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output<P> output, Context<P> context) throws Exception
    {
        try {
            this.retry.executeCallable(() -> executeSink(output, context));
        }
        catch (SinkWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
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
        return "retry." + this.sink.defaultId();
    }
}
