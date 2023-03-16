package tech.illuin.pipeline.generic.pipeline.step.execution.condition;

import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.execution.condition.StepCondition;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IsTypeB implements StepCondition
{
    @Override
    public boolean canExecute(Indexable indexable)
    {
        return indexable instanceof B;
    }
}
