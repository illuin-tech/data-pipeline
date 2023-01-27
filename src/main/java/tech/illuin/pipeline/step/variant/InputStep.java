package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface InputStep<I> extends Step<Indexable, I, Object>
{
    Result execute(I input, Context<?> context);

    default Result execute(Indexable object, I input, Object payload, Context<?> context)
    {
        return this.execute(input, context);
    }
}
