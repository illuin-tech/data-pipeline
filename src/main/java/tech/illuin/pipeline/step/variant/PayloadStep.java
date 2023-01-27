package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PayloadStep<P> extends Step<Indexable, Object, P>
{
    Result execute(P payload, Context<?> context);

    default Result execute(Indexable object, Object input, P payload, Context<?> context)
    {
        return this.execute(payload, context);
    }
}
