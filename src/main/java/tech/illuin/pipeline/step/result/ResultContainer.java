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
    private final Map<String, List<Result>> results;

    public ResultContainer()
    {
        this.createdAt = Instant.now();
        this.results = new HashMap<>();
    }

    public Instant createdAt()
    {
        return createdAt;
    }

    public ResultContainer register(String uid, Result result)
    {
        if (!this.results.containsKey(uid))
            this.results.put(uid, new ArrayList<>());
        this.results.get(uid).add(result);
        return this;
    }

    public void register(ResultContainer results)
    {
        results.results.forEach((key, value) -> {
            for (Result result : value)
                this.register(key, result);
        });
    }

    public void register(ResultView view)
    {
        ScopedResultView scoped = view.self();
        scoped.stream().forEach(result -> this.register(scoped.uid(), result));
    }

    public Stream<Result> stream(Indexable indexable)
    {
        return this.stream(indexable.uid());
    }

    public Stream<Result> stream(String uid)
    {
        return this.results.getOrDefault(uid, Collections.emptyList()).stream();
    }

    public Stream<Result> stream()
    {
        return this.results.values().stream().flatMap(Collection::stream);
    }

    public <R extends Result> Stream<R> stream(Class<R> type)
    {
        return this.stream().filter(type::isInstance).map(type::cast);
    }

    public <R extends Result> Optional<R> latest(Class<R> type)
    {
        return this.stream()
            .filter(type::isInstance)
            .max(Comparator.comparing(Result::createdAt))
            .map(type::cast)
        ;
    }

    public <E extends Enum<E>> Optional<Result> latest(E type)
    {
        return this.latest(type.name());
    }

    public Optional<Result> latest(String type)
    {
        return this.stream()
            .filter(r -> Objects.equals(r.type(), type))
            .max(Comparator.comparing(Result::createdAt))
        ;
    }

    public Stream<Result> current()
    {
        return this.stream().filter(r -> r.createdAt().isAfter(this.createdAt()));
    }

    public <R extends Result> Optional<R> current(Class<R> type)
    {
        return this.current()
            .filter(type::isInstance)
            .max(Comparator.comparing(Result::createdAt))
            .map(type::cast)
        ;
    }

    public <E extends Enum<E>> Optional<Result> current(E type)
    {
        return this.current(type.name());
    }

    public Optional<Result> current(String type)
    {
        return this.current()
            .filter(r -> Objects.equals(r.type(), type))
            .max(Comparator.comparing(Result::createdAt))
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
