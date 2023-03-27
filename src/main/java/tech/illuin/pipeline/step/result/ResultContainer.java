package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ResultContainer implements Results
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
        ScopedResults scoped = view.self();
        scoped.descriptors().stream().forEach(result -> this.register(scoped.uid(), result));
    }

    @Override
    public ResultDescriptors descriptors()
    {
        return new GlobalResultDescriptors(this);
    }

    Stream<ResultDescriptor<?>> descriptorStream()
    {
        return this.results.values().stream().flatMap(Collection::stream);
    }

    Stream<ResultDescriptor<?>> descriptorStream(String uid)
    {
        if (!this.results.containsKey(uid))
            return Stream.empty();
        return this.results.get(uid).stream();
    }

    @Override
    public Instant currentStart()
    {
        return this.createdAt;
    }

    public int size()
    {
        return this.results.size();
    }

    public ResultView view(Indexable indexable)
    {
        return new GlobalResultView(indexable.uid(), this);
    }

    public ResultView view(String uid)
    {
        return new GlobalResultView(uid, this);
    }

    public Results of(Indexable indexable)
    {
        return new ScopedResults(indexable.uid(), this);
    }

    public Results of(String uid)
    {
        return new ScopedResults(uid, this);
    }
}
