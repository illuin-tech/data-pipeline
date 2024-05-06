package tech.illuin.pipeline.execution.error;

import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

public interface PipelineErrorHandler
{
    Output handle(Exception exception, Output previous, Object input, Context context) throws PipelineException;

    @SuppressWarnings("IllegalCatch")
    default PipelineErrorHandler andThen(PipelineErrorHandler nextErrorHandler)
    {
        return (Exception ex, Output previous, Object input, Context context) -> {
            try {
                return this.handle(ex, previous, input, context);
            }
            catch (Exception e) {
                return nextErrorHandler.handle(e, previous, input, context);
            }
        };
    }

    static  Output wrapChecked(Exception ex, Output previous, Object input, Context context) throws PipelineException
    {
        if (ex instanceof RuntimeException re)
            throw re;
        throw new PipelineException(ex.getMessage(), ex);
    }
}
