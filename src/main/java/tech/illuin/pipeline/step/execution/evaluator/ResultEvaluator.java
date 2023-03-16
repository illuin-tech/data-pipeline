package tech.illuin.pipeline.step.execution.evaluator;

import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultEvaluator
{
    StepStrategy evaluate(Result result);

    ResultEvaluator ALWAYS_CONTINUE = ResultEvaluator::alwaysContinue;
    ResultEvaluator ALWAYS_ABORT = ResultEvaluator::alwaysAbort;
    ResultEvaluator ALWAYS_STOP = ResultEvaluator::alwaysStop;
    ResultEvaluator ALWAYS_DISCARD = ResultEvaluator::alwaysDiscard;

    private static StepStrategy alwaysContinue(Result result)
    {
        return StepStrategy.CONTINUE;
    }

    private static StepStrategy alwaysAbort(Result result)
    {
        return StepStrategy.ABORT;
    }

    private static StepStrategy alwaysStop(Result result)
    {
        return StepStrategy.STOP;
    }

    private static StepStrategy alwaysDiscard(Result result)
    {
        return StepStrategy.DISCARD_AND_CONTINUE;
    }
}
