package tech.illuin.pipeline.sink.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkException;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class SinkDescriptor<T>
{
    private final String id;
    private final Sink<T> sink;
    private final boolean isAsync;
    private final SinkWrapper<T> executionWrapper;
    private final SinkErrorHandler errorHandler;

    SinkDescriptor(
        String id,
        Sink<T> sink,
        boolean isAsync,
        SinkWrapper<T> executionWrapper,
        SinkErrorHandler errorHandler
    ) {
        this.id = id;
        this.sink = sink;
        this.isAsync = isAsync;
        this.executionWrapper = executionWrapper;
        this.errorHandler = errorHandler;
    }

    public void execute(Output<T> output) throws SinkException
    {
        this.executionWrapper.wrap(this.sink).execute(output);
    }

    public void handleException(Exception ex, Context<T> ctx) throws SinkException
    {
        this.errorHandler.handle(ex, ctx);
    }

    public void handleExceptionThenSwallow(Exception ex, Context<T> ctx)
    {
        try {
            this.handleException(ex, ctx);
        }
        catch (SinkException ignore) {}
    }

    public String id()
    {
        return id;
    }

    public boolean isAsync()
    {
        return this.isAsync;
    }
}
