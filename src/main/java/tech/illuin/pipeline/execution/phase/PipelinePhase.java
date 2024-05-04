package tech.illuin.pipeline.execution.phase;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PipelinePhase<I> extends AutoCloseable
{
    PipelineStrategy run(I input, Output output, Context context, MetricTags tags) throws Exception;

    default void close() throws Exception {}
}
