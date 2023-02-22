package tech.illuin.pipeline;

import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
class PipelineResult<P> extends Result
{
    private final Output<P> output;

    PipelineResult(Output<P> output)
    {
        this.output = output;
    }

    public Output<P> output()
    {
        return output;
    }
}
