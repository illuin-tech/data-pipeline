package tech.illuin.pipeline.input;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record Input<T, P>(
    T input,
    P payload
) {}
