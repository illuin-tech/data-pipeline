package tech.illuin.pipeline;

import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
record PipelineResult<P>(
    Output<P> output
) implements Result {}
