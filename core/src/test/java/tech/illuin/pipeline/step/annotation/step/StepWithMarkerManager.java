package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithMarkerManager
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithMarkerManager.class);

    @StepConfig(id = "step-with_marker-manager")
    public Result execute(MarkerManager marker)
    {
        logger.info("markers={} tags={} discriminants={}", marker.markers(), marker.tags(), marker.discriminants());
        return new TestResult("annotation-test", marker.getClass().getSimpleName());
    }
}
