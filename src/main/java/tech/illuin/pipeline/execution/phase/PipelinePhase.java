package tech.illuin.pipeline.execution.phase;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PipelinePhase<I, P>
{
    PipelineStrategy run(I input, Output<P> output, Context<P> context) throws PhaseException;
}
