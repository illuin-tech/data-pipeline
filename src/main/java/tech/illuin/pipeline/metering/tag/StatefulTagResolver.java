package tech.illuin.pipeline.metering.tag;

import tech.illuin.pipeline.context.Context;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class StatefulTagResolver<T> implements TagResolver<T>
{
    private final TagResolver<T> subResolver;
    private MetricTags tags;

    public StatefulTagResolver(TagResolver<T> subResolver)
    {
        this.subResolver = subResolver;
    }

    @Override
    public synchronized MetricTags resolve(T input, Context<?> context)
    {
        if (this.tags == null)
            this.tags = this.subResolver.resolve(input, context);
        return this.tags;
    }
}
