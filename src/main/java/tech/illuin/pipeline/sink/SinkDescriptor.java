package tech.illuin.pipeline.sink;

import tech.illuin.pipeline.commons.Reflection;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record SinkDescriptor<T>(
    Sink<T> sink,
    boolean isAsync
) {
    public String id()
    {
        Class<?> c = this.sink().getClass();
        return Reflection.isAnonymousImplementation(c) ? "anonymous-sink" : c.getName();
    }
}
