package tech.illuin.pipeline.execution.phase;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.metering.tag.MetricTags;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PipelinePhase<I> extends AutoCloseable
{
    PipelineStrategy run(IO<I> io, Context context, MetricTags tags) throws Exception;

    default void close() throws Exception {}
}
