package tech.illuin.pipeline.sink.execution.wrapper.timelimiter;

import tech.illuin.pipeline.sink.SinkException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterSinkException extends RuntimeException
{
    public TimeLimiterSinkException(SinkException ex)
    {
        super(ex.getMessage(), ex);
    }

    @Override
    public SinkException getCause()
    {
        return (SinkException) super.getCause();
    }
}
