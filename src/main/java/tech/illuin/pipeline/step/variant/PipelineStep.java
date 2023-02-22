package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.annotation.Experimental;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.function.Function;

import static tech.illuin.pipeline.input.author_resolver.AuthorResolver.ANONYMOUS;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Experimental
public class PipelineStep<I, P> implements Step<Indexable, I, VoidPayload>
{
    private final Pipeline<I, P> pipeline;
    private final Function<Output<P>, Result> resultMapper;

    public PipelineStep(Pipeline<I, P> pipeline, Function<Output<P>, Result> resultMapper)
    {
        this.pipeline = pipeline;
        this.resultMapper = resultMapper;
    }

    @Override
    public Result execute(Indexable object, I input, VoidPayload payload, ResultView results, Context<VoidPayload> context) throws StepException
    {
        try {
            /* TODO: Bad design -> this should be challenged ASAP */
            Output<P> prev = new Output<>(results.self().uid(), ANONYMOUS);
            prev.setPayload(null);
            prev.results().register(results);
            Output<P> out = this.pipeline.run(input, new SimpleContext<>(prev));
            return resultMapper.apply(out);
        }
        catch (PipelineException e) {
            throw new StepException(e.getMessage(), e);
        }
    }

    @Override
    public String defaultId()
    {
        return this.pipeline.id();
    }
}
