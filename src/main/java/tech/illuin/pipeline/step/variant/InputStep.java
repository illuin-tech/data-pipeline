package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface InputStep<I> extends Step<Indexable, I, Object>
{
    Result execute(I input, ResultView results, Context<?> context) throws StepException;

    default Result execute(Indexable object, I input, Object payload, ResultView results, Context<Object> context) throws StepException
    {
        return this.execute(input, results, context);
    }
}