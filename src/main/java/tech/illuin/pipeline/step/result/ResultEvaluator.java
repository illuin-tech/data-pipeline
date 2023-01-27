package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.step.StepStrategy;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultEvaluator
{
    StepStrategy evaluate(Result result);
}
