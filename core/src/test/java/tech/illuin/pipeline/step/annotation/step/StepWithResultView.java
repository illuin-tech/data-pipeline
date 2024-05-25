package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithResultView
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithResultView.class);

    @StepConfig(id = "step-with_resultview")
    public Result execute(ResultView resultView)
    {
        logger.info("result_view: {}", resultView);
        return new TestResult("annotation-test", resultView.latest(TestResult.class).map(TestResult::status).orElse(null));
    }
}
