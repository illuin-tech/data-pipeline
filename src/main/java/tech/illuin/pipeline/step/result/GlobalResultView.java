package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class GlobalResultView implements ResultView
{
    private final String uid;
    private final ResultContainer container;

    GlobalResultView(Indexable indexable, ResultContainer container)
    {
        this.uid = indexable.uid();
        this.container = container;
    }

    @Override
    public Stream<Result> stream()
    {
        return this.container.stream();
    }

    @Override
    public Stream<ResultDescriptor<?>> descriptors()
    {
        return this.container.descriptors();
    }

    @Override
    public Instant currentStart()
    {
        return this.container.createdAt();
    }

    @Override
    public ScopedResultView self()
    {
        return new ScopedResultView(this.uid, this.container);
    }
}
