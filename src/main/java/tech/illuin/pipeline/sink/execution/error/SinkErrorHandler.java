package tech.illuin.pipeline.sink.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.sink.SinkException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface SinkErrorHandler
{
    void handle(Exception exception, Context<?> context) throws SinkException;

    SinkErrorHandler RETHROW_ALL = SinkErrorHandler::rethrowAll;

    private static void rethrowAll(Exception ex, Context<?> ctx) throws SinkException
    {
        if (ex instanceof SinkException sinkEx)
            throw sinkEx;
        if (ex instanceof RuntimeException runEx)
            throw runEx;
        throw new RuntimeException("Unexpected error type found in handler: " + ex.getClass().getName() + " with message " + ex.getMessage());
    }
}
