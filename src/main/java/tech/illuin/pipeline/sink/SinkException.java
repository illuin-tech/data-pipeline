package tech.illuin.pipeline.sink;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkException extends Exception
{
    public SinkException(String message)
    {
        super(message);
    }

    public SinkException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
