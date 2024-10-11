package tech.illuin.pipeline.step.execution.evaluator;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.execution.interruption.Interruption;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultEvaluator
{
    StepStrategy evaluate(Result result, Indexable object, Object input, Context ctx);

    ResultEvaluator ALWAYS_CONTINUE = ResultEvaluator::alwaysContinue;
    ResultEvaluator ALWAYS_SKIP = ResultEvaluator::alwaysSkip;
    ResultEvaluator ALWAYS_ABORT = ResultEvaluator::alwaysAbort;
    ResultEvaluator ALWAYS_STOP = ResultEvaluator::alwaysStop;
    ResultEvaluator ALWAYS_DISCARD = ResultEvaluator::alwaysDiscard;
    ResultEvaluator SKIP_ON_INTERRUPT = ResultEvaluator::skipOnInterrupt;
    ResultEvaluator ABORT_ON_INTERRUPT = ResultEvaluator::abortOnInterrupt;
    ResultEvaluator STOP_ON_INTERRUPT = ResultEvaluator::stopOnInterrupt;
    ResultEvaluator DISCARD_ON_INTERRUPT = ResultEvaluator::discardOnInterrupt;

    private static StepStrategy alwaysContinue(Result result, Indexable object, Object input, Context ctx)
    {
        return StepStrategy.CONTINUE;
    }

    private static StepStrategy alwaysSkip(Result result, Indexable object, Object input, Context ctx)
    {
        return StepStrategy.SKIP;
    }

    private static StepStrategy alwaysAbort(Result result, Indexable object, Object input, Context ctx)
    {
        return StepStrategy.ABORT;
    }

    private static StepStrategy alwaysStop(Result result, Indexable object, Object input, Context ctx)
    {
        return StepStrategy.STOP;
    }

    private static StepStrategy alwaysDiscard(Result result, Indexable object, Object input, Context ctx)
    {
        return StepStrategy.DISCARD_AND_CONTINUE;
    }

    private static StepStrategy skipOnInterrupt(Result result, Indexable object, Object input, Context ctx)
    {
        if (result instanceof Interruption)
            return StepStrategy.SKIP;
        return StepStrategy.CONTINUE;
    }

    private static StepStrategy abortOnInterrupt(Result result, Indexable object, Object input, Context ctx)
    {
        if (result instanceof Interruption)
            return StepStrategy.ABORT;
        return StepStrategy.CONTINUE;
    }

    private static StepStrategy stopOnInterrupt(Result result, Indexable object, Object input, Context ctx)
    {
        if (result instanceof Interruption)
            return StepStrategy.STOP;
        return StepStrategy.CONTINUE;
    }

    private static StepStrategy discardOnInterrupt(Result result, Indexable object, Object input, Context ctx)
    {
        if (result instanceof Interruption)
            return StepStrategy.DISCARD_AND_CONTINUE;
        return StepStrategy.CONTINUE;
    }
}
