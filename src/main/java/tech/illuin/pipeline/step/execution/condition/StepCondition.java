package tech.illuin.pipeline.step.execution.condition;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface StepCondition
{
    boolean canExecute(Indexable indexable);

    StepCondition ALWAYS_TRUE = StepCondition::alwaysTrue;
    StepCondition ALWAYS_FALSE = StepCondition::alwaysFalse;

    /* Default implementations */

    private static boolean alwaysTrue(Indexable indexable)
    {
        return true;
    }

    private static boolean alwaysFalse(Indexable indexable)
    {
        return false;
    }
}
