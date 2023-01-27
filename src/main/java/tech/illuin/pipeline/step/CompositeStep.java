package tech.illuin.pipeline.step;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultEvaluator;

import java.util.function.Predicate;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
final class CompositeStep<T extends Indexable, I, P> implements Step<T, I, P>
{
    private final Step<T, I, P> step;
    private final Predicate<Indexable> predicate;
    private final ResultEvaluator resultEvaluator;

    CompositeStep(
        Step<T, I, P> step,
        Predicate<Indexable> predicate,
        ResultEvaluator resultEvaluator
    ) {
        this.step = step;
        this.predicate = predicate;
        this.resultEvaluator = resultEvaluator;
    }

    @Override
    public Result execute(T data, I input, P payload, Context<?> context)
    {
        return this.step.execute(data, input, payload, context);
    }

    @Override
    public boolean canExecute(Indexable indexable)
    {
        return this.predicate.test(indexable);
    }

    @Override
    public StepStrategy postEvaluation(Result result)
    {
        return this.resultEvaluator.evaluate(result);
    }

    @Override
    public String id()
    {
        return this.step.id();
    }
}
