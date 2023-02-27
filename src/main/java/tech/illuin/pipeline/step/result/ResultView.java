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

    default Stream<Result> stream(String type)
    {
        return this.stream().filter(r -> Objects.equals(r.type(), type));
    }

    default <R extends Result> Optional<R> latest(Class<R> type)
    {
        return this.stream()
            .filter(type::isInstance)
            .max(Comparator.comparing(Result::createdAt))
            .map(type::cast)
        ;
    }

    default <E extends Enum<E>> Optional<Result> latest(E type)
    {
        return this.latest(type.name());
    }

    default Optional<Result> latest(String type)
    {
        return this.stream()
           .filter(r -> Objects.equals(r.type(), type))
           .max(Comparator.comparing(Result::createdAt))
        ;
    }

    default <R extends Result> Optional<R> current(Class<R> type)
    {
        return this.stream()
            .filter(r -> r.createdAt().isAfter(this.currentStart()))
            .filter(type::isInstance)
            .max(Comparator.comparing(Result::createdAt))
            .map(type::cast)
        ;
    }

    default <E extends Enum<E>> Optional<Result> current(E type)
    {
        return this.current(type.name());
    }

    default Optional<Result> current(String type)
    {
        return this.stream()
            .filter(r -> r.createdAt().isAfter(this.currentStart()))
            .filter(r -> Objects.equals(r.type(), type))
            .max(Comparator.comparing(Result::createdAt))
        ;
    }
}
