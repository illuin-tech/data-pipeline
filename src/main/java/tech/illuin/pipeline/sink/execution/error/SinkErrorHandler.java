package tech.illuin.pipeline.sink.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface SinkErrorHandler
{
    void handle(Exception exception, Output<?> output, Context<?> context) throws Exception;

    SinkErrorHandler RETHROW_ALL = (Exception ex, Output<?> output, Context<?> ctx) -> {
        throw ex;
    };

    @SuppressWarnings("IllegalCatch")
    default SinkErrorHandler andThen(SinkErrorHandler nextErrorHandler)
    {
        return (Exception exception, Output<?> output, Context<?> context) -> {
            try {
                handle(exception, output, context);
            }
            catch (Exception e) {
                nextErrorHandler.handle(e, output, context);
            }
        };
    }
}
