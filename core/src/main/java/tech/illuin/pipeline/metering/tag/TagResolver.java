package tech.illuin.pipeline.metering.tag;

import tech.illuin.pipeline.context.Context;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface TagResolver<T>
{
    void resolve(MetricTags tags, T input, Context context);
}
