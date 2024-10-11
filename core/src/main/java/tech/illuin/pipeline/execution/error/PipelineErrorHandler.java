package tech.illuin.pipeline.execution.error;

import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

public interface PipelineErrorHandler
{
    Output handle(Exception exception, Output previous, Object input, Context context, PipelineTag tag) throws PipelineException;

    @SuppressWarnings("IllegalCatch")
    default PipelineErrorHandler andThen(PipelineErrorHandler nextErrorHandler)
    {
        return (Exception ex, Output previous, Object input, Context context, PipelineTag tag) -> {
            try {
                return this.handle(ex, previous, input, context, tag);
            }
            catch (Exception e) {
                return nextErrorHandler.handle(e, previous, input, context, tag);
            }
        };
    }

    static Output wrapChecked(Exception ex, Output previous, Object input, Context context, PipelineTag tag) throws PipelineException
    {
        if (ex instanceof RuntimeException re)
            throw re;
        throw new PipelineException(tag, context, ex.getMessage(), ex);
    }
}
