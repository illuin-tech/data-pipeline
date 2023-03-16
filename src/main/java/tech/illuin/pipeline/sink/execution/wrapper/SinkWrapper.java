package tech.illuin.pipeline.sink.execution.wrapper;

import tech.illuin.pipeline.sink.Sink;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface SinkWrapper<P>
{
    Sink<P> wrap(Sink<P> sink);

    static <P> Sink<P> noOp(Sink<P> sink)
    {
        return sink;
    }
}
