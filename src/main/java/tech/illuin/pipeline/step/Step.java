package tech.illuin.pipeline.step;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultEvaluator;
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.util.function.Predicate;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Step<T extends Indexable, I, P>
{
    Result execute(T object, I input, P payload, Context<?> context);

    default StepStrategy postEvaluation(Result result)
    {
        return StepStrategy.CONTINUE;
    }

    default boolean canExecute(Indexable indexable)
    {
        return true;
    }

    default String id()
    {
        return this.getClass().getName();
    }

    final class Builder<T extends Indexable, I, P>
    {
        private Step<? extends T, I, P> step;
        private Predicate<Indexable> executionCondition;
        private ResultEvaluator resultEvaluator;

        public Builder()
        {
            this.step = null;
            this.executionCondition = any -> true;
            this.resultEvaluator = any -> StepStrategy.CONTINUE;
        }

        public Builder<T, I, P> step(Step<? extends T, I, P> step)
        {
            this.step = step;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder<T, I, P> step(IndexableStep<? extends T> step)
        {
            this.step = (Step<T, I, P>) step;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder<T, I, P> step(InputStep<I> step)
        {
            this.step = (Step<T, I, P>) step;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder<T, I, P> step(PayloadStep<P> step)
        {
            this.step = (Step<T, I, P>) step;
            return this;
        }

        public Builder<T, I, P> withCondition(Class<? extends Indexable> typeCondition)
        {
            this.executionCondition = typeCondition::isInstance;
            return this;
        }

        public Builder<T, I, P> withCondition(Predicate<Indexable> predicate)
        {
            this.executionCondition = predicate;
            return this;
        }

        public Builder<T, I, P> withEvaluation(ResultEvaluator evaluator)
        {
            this.resultEvaluator = evaluator;
            return this;
        }

        @SuppressWarnings("unchecked")
        Step<T, I, P> build()
        {
            return (Step<T, I, P>) new CompositeStep<>(
                this.step,
                this.executionCondition,
                this.resultEvaluator
            );
        }
    }
}
