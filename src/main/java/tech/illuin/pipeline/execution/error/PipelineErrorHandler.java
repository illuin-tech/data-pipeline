package tech.illuin.pipeline.execution.error;

import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

public interface PipelineErrorHandler<P>
{
    Output<P> handle(Exception exception, Output<P> previous, Object input, Context<P> context) throws PipelineException;

    @SuppressWarnings("IllegalCatch")
    default PipelineErrorHandler<P> andThen(PipelineErrorHandler<P> nextErrorHandler)
    {
        return (Exception ex, Output<P> previous, Object input, Context<P> context) -> {
            try {
                return this.handle(ex, previous, input, context);
            }
            catch (Exception e) {
                return nextErrorHandler.handle(e, previous, input, context);
            }
        };
    }

    static <P> Output<P> wrapChecked(Exception ex, Output<P> previous, Object input, Context<P> context) throws PipelineException
    {
        if (ex instanceof RuntimeException re)
            throw re;
        throw new PipelineException(ex.getMessage(), ex);
    }
}
