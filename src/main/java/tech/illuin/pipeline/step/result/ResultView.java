package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultView
{
    Stream<Result> stream();

    Stream<ResultDescriptor<?>> descriptors();

    Instant currentStart();

    ScopedResultView self();

    default <R extends Result> Stream<R> stream(Class<R> type)
    {
        return this.stream().filter(type::isInstance).map(type::cast);
    }

    default <E extends Enum<E>> Stream<Result> stream(E type)
    {
        return this.stream(type.name());
    }

    default Stream<Result> stream(String name)
    {
        return this.stream().filter(r -> Objects.equals(r.name(), name));
    }

    default <R extends Result> Optional<R> latest(Class<R> type)
    {
        return this.descriptors()
            .filter(rd -> type.isInstance(rd.payload()))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
            .map(type::cast)
        ;
    }

    default <E extends Enum<E>> Optional<Result> latest(E type)
    {
        return this.latest(type.name());
    }

    default Optional<Result> latest(String name)
    {
        return this.descriptors()
            .filter(rd -> Objects.equals(rd.payload().name(), name))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
        ;
    }

    default <R extends Result> Optional<R> current(Class<R> type)
    {
        return this.descriptors()
            .filter(r -> r.createdAt().isAfter(this.currentStart()))
            .filter(rd -> type.isInstance(rd.payload()))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
            .map(type::cast)
        ;
    }

    default <E extends Enum<E>> Optional<Result> current(E type)
    {
        return this.current(type.name());
    }

    default Optional<Result> current(String name)
    {
        return this.descriptors()
            .filter(rd -> rd.createdAt().isAfter(this.currentStart()))
            .filter(rd -> Objects.equals(rd.payload().name(), name))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
        ;
    }
}
