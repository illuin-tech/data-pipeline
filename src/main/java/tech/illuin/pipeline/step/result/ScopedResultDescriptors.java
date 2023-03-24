package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class ScopedResultDescriptors implements ResultDescriptors
{
    private final String uid;
    private final ResultContainer container;

    ScopedResultDescriptors(String uid, ResultContainer container)
    {
        this.uid = uid;
        this.container = container;
    }

    public String uid()
    {
        return this.uid;
    }

    @Override
    public Stream<ResultDescriptor<?>> stream()
    {
        return this.container.descriptorStream(this.uid);
    }

    @Override
    public Instant currentStart()
    {
        return this.container.createdAt();
    }
}
