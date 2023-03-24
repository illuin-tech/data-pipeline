package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Results
{
    ResultDescriptors descriptors();

    Instant currentStart();

    default Stream<Result> stream()
    {
        return this.descriptors().stream().map(ResultDescriptor::result);
    }

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
        return this.descriptors().stream().filter(rd -> Objects.equals(rd.result().name(), name)).map(ResultDescriptor::result);
    }

    default <R extends Result> Optional<R> latest(Class<R> type)
    {
        return this.descriptors().latest(type).map(ResultDescriptor::result).map(type::cast);
    }

    default <E extends Enum<E>> Optional<Result> latest(E type)
    {
        return this.latest(type.name());
    }

    default Optional<Result> latest(String name)
    {
        return this.descriptors().latest(name).map(ResultDescriptor::result);
    }

    default Stream<Result> current()
    {
        return this.descriptors().current().map(ResultDescriptor::result);
    }

    default <R extends Result> Optional<R> current(Class<R> type)
    {
        return this.descriptors().current(type).map(ResultDescriptor::result).map(type::cast);
    }

    default <E extends Enum<E>> Optional<Result> current(E type)
    {
        return this.current(type.name());
    }

    default Optional<Result> current(String name)
    {
        return this.descriptors().current(name).map(ResultDescriptor::result);
    }
}
