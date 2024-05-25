package tech.illuin.pipeline.step.result;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class GlobalResultDescriptors implements ResultDescriptors
{
    private final ResultContainer container;

    GlobalResultDescriptors(ResultContainer container)
    {
        this.container = container;
    }

    @Override
    public Stream<ResultDescriptor<?>> stream()
    {
        return this.container.descriptorStream();
    }

    @Override
    public Instant currentStart()
    {
        return this.container.createdAt();
    }
}
