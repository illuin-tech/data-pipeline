package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithObservabilityManager
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithObservabilityManager.class);

    @StepConfig(id = "step-with_observability-manager")
    public Result execute(ObservabilityManager manager)
    {
        logger.info("observability-manager: meterRegistry={} tracer={}", manager.meterRegistry().getClass().getName(), manager.tracer().getClass().getName());
        return new TestResult("annotation-test", manager.meterRegistry().getClass().getSimpleName());
    }
}
