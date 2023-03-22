package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ResultContainer
{
    private final Instant createdAt;
    private final Map<String, List<ResultDescriptor<?>>> results;

    public ResultContainer()
    {
        this.createdAt = Instant.now();
        this.results = new HashMap<>();
    }

    public Instant createdAt()
    {
        return this.createdAt;
    }

    public ResultContainer register(String uid, ResultDescriptor<?> result)
    {
        if (!this.results.containsKey(uid))
            this.results.put(uid, new ArrayList<>());
        this.results.get(uid).add(result);
        return this;
    }

    public void register(ResultContainer results)
    {
        results.results.forEach((key, value) -> {
            for (ResultDescriptor<?> result : value)
                this.register(key, result);
        });
    }

    public void register(ResultView view)
    {
        ScopedResultView scoped = view.self();
        scoped.descriptors().forEach(result -> this.register(scoped.uid(), result));
    }

    public Stream<ResultDescriptor<?>> descriptors(Indexable indexable)
    {
        return this.descriptors(indexable.uid());
    }

    public Stream<ResultDescriptor<?>> descriptors(String uid)
    {
        return this.results.getOrDefault(uid, Collections.emptyList()).stream();
    }

    public Stream<ResultDescriptor<?>> descriptors()
    {
        return this.results.values().stream().flatMap(Collection::stream);
    }

    public Stream<ResultDescriptor<?>> currentDescriptors()
    {
        return this.descriptors().filter(rd -> rd.createdAt().isAfter(this.createdAt()));
    }

    public Stream<Result> stream(Indexable indexable)
    {
        return this.descriptors(indexable).map(ResultDescriptor::payload);
    }

    public Stream<Result> stream(String uid)
    {
        return this.descriptors(uid).map(ResultDescriptor::payload);
    }

    public Stream<Result> stream()
    {
        return this.descriptors().map(ResultDescriptor::payload);
    }

    public <R> Stream<R> stream(Class<R> type)
    {
        return this.stream().filter(type::isInstance).map(type::cast);
    }

    public <R> Optional<R> latest(Class<R> type)
    {
        return this.descriptors()
            .filter(rd -> type.isInstance(rd.payload()))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
            .map(type::cast)
        ;
    }

    public <E extends Enum<E>> Optional<Result> latest(E name)
    {
        return this.latest(name.name());
    }

    public Optional<Result> latest(String name)
    {
        return this.descriptors()
            .filter(rd -> Objects.equals(rd.payload().name(), name))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
        ;
    }

    public Stream<Result> current()
    {
        return this.currentDescriptors().map(ResultDescriptor::payload);
    }

    public <R> Optional<R> current(Class<R> type)
    {
        return this.currentDescriptors()
            .filter(rd -> type.isInstance(rd.payload()))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
            .map(type::cast)
        ;
    }

    public <E extends Enum<E>> Optional<Result> current(E type)
    {
        return this.current(type.name());
    }

    public Optional<Result> current(String name)
    {
        return this.currentDescriptors()
            .filter(rd -> Objects.equals(rd.payload().name(), name))
            .max(Comparator.comparing(ResultDescriptor::createdAt))
            .map(ResultDescriptor::payload)
        ;
    }

    public int size()
    {
        return this.results.size();
    }

    public ResultView view(Indexable indexable)
    {
        return new GlobalResultView(indexable, this);
    }
}
