package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface PayloadStep<P> extends Step<Indexable, Object, P>
{
    Result execute(P payload, ResultView results, Context<P> context) throws Exception;

    default Result execute(Indexable object, Object input, P payload, ResultView results, Context<P> context) throws Exception
    {
        return this.execute(payload, results, context);
    }
}
