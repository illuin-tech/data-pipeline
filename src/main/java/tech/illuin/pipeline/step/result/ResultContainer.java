package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ResultContainer
{
    private final Map<UUID, List<Result>> results;

    public ResultContainer()
    {
        this.results = new HashMap<>();
    }

    public ResultContainer register(UUID uid, Result result)
    {
        if (!this.results.containsKey(uid))
            this.results.put(uid, new ArrayList<>());
        this.results.get(uid).add(result);
        return this;
    }

    public Stream<Result> stream(Indexable indexable)
    {
        return this.stream(indexable.uid());
    }

    public Stream<Result> stream(UUID uid)
    {
        return this.results.getOrDefault(uid, Collections.emptyList()).stream();
    }
}
