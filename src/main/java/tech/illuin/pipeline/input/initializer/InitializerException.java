package tech.illuin.pipeline.input.initializer;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 * @deprecated slated for removal by 0.12
 */
@Deprecated
public class InitializerException extends Exception
{
    public InitializerException(String message)
    {
        super(message);
    }

    public InitializerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InitializerException(Throwable cause)
    {
        super(cause);
    }
}
