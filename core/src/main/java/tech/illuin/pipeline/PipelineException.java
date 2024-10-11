package tech.illuin.pipeline;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineException extends Exception
{
    private final PipelineTag tag;
    private final Context context;

    public PipelineException(PipelineTag tag, Context context, String message, Throwable cause)
    {
        super(message, cause);
        this.tag = tag;
        this.context = context;
    }

    public PipelineTag tag()
    {
        return this.tag;
    }

    public Context context()
    {
        return this.context;
    }
}
