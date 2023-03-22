package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultDescriptors
{
    Stream<ResultDescriptor<?>> stream();

    Instant currentStart();

    @SuppressWarnings("unchecked")
    default <R extends Result> Stream<ResultDescriptor<R>> stream(Class<R> type)
    {
        return this.stream().filter(rd -> type.isInstance(rd.payload())).map(rd -> (ResultDescriptor<R>) rd);
    }

    default <E extends Enum<E>> Stream<ResultDescriptor<?>> stream(E type)
    {
        return this.stream(type.name());
    }

    default Stream<ResultDescriptor<?>> stream(String name)
    {
        return this.stream().filter(rd -> Objects.equals(rd.payload().name(), name));
    }

    @SuppressWarnings("unchecked")
    default <R extends Result> Optional<ResultDescriptor<R>> latest(Class<R> type)
    {
        return this.stream()
           .filter(rd -> type.isInstance(rd.payload()))
           .max(Comparator.comparing(ResultDescriptor::createdAt))
           .map(rd -> (ResultDescriptor<R>) rd)
        ;
    }

    default <E extends Enum<E>> Optional<ResultDescriptor<?>> latest(E type)
    {
        return this.latest(type.name());
    }

    default Optional<ResultDescriptor<?>> latest(String name)
    {
        return this.stream()
           .filter(rd -> Objects.equals(rd.payload().name(), name))
           .max(Comparator.comparing(ResultDescriptor::createdAt))
        ;
    }

    default Stream<ResultDescriptor<?>> current()
    {
        return this.stream().filter(r -> r.createdAt().isAfter(this.currentStart()));
    }

    @SuppressWarnings("unchecked")
    default <R extends Result> Optional<ResultDescriptor<R>> current(Class<R> type)
    {
        return this.current()
            .filter(rd -> type.isInstance(rd.payload()))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(rd -> (ResultDescriptor<R>) rd)
        ;
    }

    default <E extends Enum<E>> Optional<ResultDescriptor<?>> current(E type)
    {
        return this.current(type.name());
    }

    default Optional<ResultDescriptor<?>> current(String name)
    {
        return this.current()
           .filter(rd -> Objects.equals(rd.payload().name(), name))
           .max(Comparator.comparing(ResultDescriptor::createdAt))
        ;
    }
}
