package tech.illuin.pipeline.sink;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record SinkDescriptor<T>(
    Sink<T> sink,
    boolean isAsync
) {}
