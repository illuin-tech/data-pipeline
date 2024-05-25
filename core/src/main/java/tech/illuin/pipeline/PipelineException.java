package tech.illuin.pipeline;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineException extends Exception
{
    public PipelineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
