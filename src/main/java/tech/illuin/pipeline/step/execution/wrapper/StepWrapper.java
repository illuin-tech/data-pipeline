package tech.illuin.pipeline.step.execution.wrapper;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface StepWrapper<T extends Indexable, I, P>
{
    Step<T, I, P> wrap(Step<T, I, P> step);

    static <T extends Indexable, I, P> Step<T, I, P> noOp(Step<T, I, P> step)
    {
        return step;
    }

    default StepWrapper<T, I, P> andThen(StepWrapper<T, I, P> nextWrapper)
    {
        return step -> nextWrapper.wrap(this.wrap(step));
    }
}
