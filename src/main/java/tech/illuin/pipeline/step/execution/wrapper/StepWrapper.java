package tech.illuin.pipeline.step.execution.wrapper;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface StepWrapper<T extends Indexable, I>
{
    Step<T, I> wrap(Step<T, I> step);

    static <T extends Indexable, I> Step<T, I> noOp(Step<T, I> step)
    {
        return step;
    }

    default StepWrapper<T, I> andThen(StepWrapper<T, I> nextWrapper)
    {
        return step -> nextWrapper.wrap(this.wrap(step));
    }
}
