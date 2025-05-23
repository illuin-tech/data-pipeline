package tech.illuin.pipeline.sink.builder;

import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class SinkDescriptor
{
    private final String id;
    private final Sink sink;
    private final boolean isAsync;
    private final SinkWrapper executionWrapper;
    private final SinkErrorHandler errorHandler;

    SinkDescriptor(
        String id,
        Sink sink,
        boolean isAsync,
        SinkWrapper executionWrapper,
        SinkErrorHandler errorHandler
    ) {
        this.id = id;
        this.sink = sink;
        this.isAsync = isAsync;
        this.executionWrapper = executionWrapper;
        this.errorHandler = errorHandler;
    }

    public void execute(Output output, LocalContext ctx) throws Exception
    {
        this.executionWrapper.wrap(this.sink).execute(output, ctx);
    }

    public void handleException(Exception ex, Output output, LocalContext ctx) throws Exception
    {
        this.errorHandler.handle(ex, output, ctx);
    }

    @SuppressWarnings("IllegalCatch")
    public void handleExceptionThenSwallow(Exception ex, Output output, LocalContext ctx)
    {
        try {
            this.handleException(ex, output, ctx);
        }
        catch (Exception ignore) {}
    }

    public String id()
    {
        return id;
    }

    public boolean isAsync()
    {
        return this.isAsync;
    }

    public Sink sink()
    {
        return this.sink;
    }

    public SinkWrapper executionWrapper()
    {
        return this.executionWrapper;
    }

    public SinkErrorHandler errorHandler()
    {
        return this.errorHandler;
    }
}
