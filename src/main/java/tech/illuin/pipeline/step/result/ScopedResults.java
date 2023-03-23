package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class ScopedResults implements Results
{
    private final String uid;
    private final ResultContainer container;

    ScopedResults(String uid, ResultContainer container)
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
        return this.container.descriptorStream(this.uid).map(ResultDescriptor::payload);
    }

    @Override
    public ResultDescriptors descriptors()
    {
        return new ScopedResultDescriptors(this.uid, this.container);
    }

    @Override
    public Instant currentStart()
    {
        return this.container.createdAt();
    }
}
