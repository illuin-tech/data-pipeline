package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class ScopedResultView implements ResultView
{
    private final String uid;
    private final ResultContainer container;

    ScopedResultView(String uid, ResultContainer container)
    {
        this.uid = uid;
        this.container = container;
    }

    public String uid()
    {
        return this.uid;
    }

    @Override
    public Stream<Result> stream()
    {
        return this.container.stream(this.uid);
    }

    @Override
    public Stream<ResultDescriptor<?>> descriptors()
    {
        return this.container.descriptors(this.uid);
    }

    @Override
    public Instant currentStart()
    {
        return this.container.createdAt();
    }

    @Override
    public ScopedResultView self()
    {
        return this;
    }
}
