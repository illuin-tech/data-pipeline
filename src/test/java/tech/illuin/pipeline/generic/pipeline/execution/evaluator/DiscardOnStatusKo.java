package tech.illuin.pipeline.generic.pipeline.execution.evaluator;

import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DiscardOnStatusKo implements ResultEvaluator
{
    @Override
    public StepStrategy evaluate(Result result)
    {
        return isKo(result) ? StepStrategy.DISCARD_AND_CONTINUE : StepStrategy.CONTINUE;
    }

    private static boolean isKo(Result result)
    {
        if (result instanceof TestResult testResult)
            return Objects.equals("ko", testResult.status());
        return false;
    }
}
