package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.annotation.Experimental;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Experimental
public final class PipelineStep<I> implements Step<Indexable, I>
{
    private final Pipeline<I> pipeline;
    private final Function<Output, Result> resultMapper;

    public static final String PARENT_PIPELINE = "parent_pipeline";

    public PipelineStep(Pipeline<I> pipeline, Function<Output, Result> resultMapper)
    {
        this.pipeline = pipeline;
        this.resultMapper = resultMapper;
    }

    @Override
    public Result execute(Indexable object, I input, Object payload, ResultView results, Context context) throws Exception
    {
        try {
            if (!(context instanceof LocalContext localContext))
                throw new IllegalArgumentException("The provided context is not a LocalContext");

            /* TODO: Bad design -> this should be challenged ASAP */
            Output prev = new Output(
                localContext.pipelineTag(),
                payload,
                context
            );
            prev.results().register(results);
            Output out = this.pipeline.run(input, new SimpleContext(prev)
                .copyFrom(context)
                .set(PARENT_PIPELINE, localContext.pipelineTag())
            );
            return this.resultMapper.apply(out);
        }
        catch (PipelineException e) {
            throw (Exception) e.getCause();
        }
    }

    @Override
    public String defaultId()
    {
        return this.pipeline.id();
    }
}
