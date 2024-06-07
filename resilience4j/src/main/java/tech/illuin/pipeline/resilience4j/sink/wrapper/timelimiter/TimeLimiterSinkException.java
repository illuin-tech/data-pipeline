package tech.illuin.pipeline.resilience4j.sink.wrapper.timelimiter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterSinkException extends RuntimeException
{
    public TimeLimiterSinkException(Exception ex)
    {
        super(ex.getMessage(), ex);
    }

    @Override
    public Exception getCause()
    {
        return (Exception) super.getCause();
    }
}
