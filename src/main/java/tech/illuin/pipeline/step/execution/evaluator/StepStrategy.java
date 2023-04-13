package tech.illuin.pipeline.step.execution.evaluator;

import java.util.Set;

import static tech.illuin.pipeline.step.execution.evaluator.StrategyBehaviour.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public enum StepStrategy
{
    /* Keep current object in the pool, continue processing */
    CONTINUE(REGISTER_RESULT),
    /* Keep current object in the pool, ignore the result, continue processing */
    SKIP,
    /* Discard all objects from the pool, remaining pinned steps are executed */
    STOP(REGISTER_RESULT, DISCARD_ALL, STOP_CURRENT),
    /* Discard current object from the pool, continue processing */
    DISCARD_AND_CONTINUE(REGISTER_RESULT, DISCARD_CURRENT),
    /* Discard all objects from the pool, stop processing immediately */
    ABORT(REGISTER_RESULT, STOP_ALL),
    /* Exits the pipeline without running remaining steps and sinks */
    EXIT(REGISTER_RESULT, EXIT_PIPELINE),
    ;

    private final Set<StrategyBehaviour> behaviours;

    StepStrategy(StrategyBehaviour... behaviours)
    {
        this.behaviours = Set.of(behaviours);
    }

    public boolean hasBehaviour(StrategyBehaviour behaviour)
    {
        return this.behaviours.contains(behaviour);
    }
}
