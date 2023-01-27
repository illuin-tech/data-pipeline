package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface IndexableStep<T extends Indexable> extends Step<T, Object, Object>
{
    Result execute(T object, Context<?> context);

    default Result execute(T object, Object input, Object payload, Context<?> context)
    {
        return this.execute(object, context);
    }
}
