package tech.illuin.pipeline.execution.phase;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PipelinePhase<I, P> extends AutoCloseable
{
    PipelineStrategy run(I input, Output<P> output, Context<P> context, MetricTags tags) throws Exception;

    default void close() throws Exception {}
}
