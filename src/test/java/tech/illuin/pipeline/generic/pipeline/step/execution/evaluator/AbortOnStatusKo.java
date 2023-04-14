package tech.illuin.pipeline.generic.pipeline.step.execution.evaluator;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AbortOnStatusKo implements ResultEvaluator
{
    @Override
    public StepStrategy evaluate(Result result, Indexable object, Object input, Context<?> ctx)
    {
        return isKo(result) ? StepStrategy.ABORT : StepStrategy.CONTINUE;
    }

    private static boolean isKo(Result result)
    {
        if (result instanceof TestResult testResult)
            return Objects.equals("ko", testResult.status());
        return false;
    }
}
