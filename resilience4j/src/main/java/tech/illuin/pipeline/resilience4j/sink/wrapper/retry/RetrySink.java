package tech.illuin.pipeline.resilience4j.sink.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryException;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapperException;

import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetrySink implements Sink
{
    private final Sink sink;
    private final Retry retry;

    public RetrySink(Sink sink, Retry retry)
    {
        this.sink = sink;
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void execute(Output output, Context context) throws Exception
    {
        try {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            this.retry.executeCallable(() -> {
                MDC.setContextMap(mdc);
                return executeSink(output, context);
            });
        }
        catch (SinkWrapperException e) {
            throw (Exception) e.getCause();
        }
        catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
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
        return "retry." + this.sink.defaultId();
    }
}
