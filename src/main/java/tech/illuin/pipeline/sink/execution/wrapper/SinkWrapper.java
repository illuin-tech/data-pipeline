package tech.illuin.pipeline.sink.execution.wrapper;

import tech.illuin.pipeline.sink.Sink;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface SinkWrapper
{
    Sink wrap(Sink sink);

    static Sink noOp(Sink sink)
    {
        return sink;
    }

    default SinkWrapper andThen(SinkWrapper nextWrapper)
    {
        return step -> nextWrapper.wrap(this.wrap(step));
    }
}
