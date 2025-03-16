package tech.illuin.pipeline.execution.phase;

import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

public class IO<I>
{
    private final PipelineTag tag;
    private final I input;
    private Output output;

    public IO(PipelineTag tag, I input)
    {
        this.tag = tag;
        this.input = input;
    }

    public PipelineTag tag()
    {
        return this.tag;
    }

    public I input()
    {
        return this.input;
    }

    public Output output()
    {
        return this.output;
    }

    public synchronized void setOutput(Output output)
    {
        if (this.output != null)
            throw new IllegalStateException("Output has already been set");

        this.output = output;
    }
}
