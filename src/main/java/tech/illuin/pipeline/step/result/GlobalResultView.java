package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class GlobalResultView implements ResultView
{
    private final String uid;
    private final ResultContainer container;

    GlobalResultView(String uid, ResultContainer container)
    {
        this.uid = uid;
        this.container = container;
    }

    @Override
    public Stream<Result> stream()
    {
        return this.container.stream();
    }

    @Override
    public ResultDescriptors descriptors()
    {
        return new GlobalResultDescriptors(this.container);
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
