package tech.illuin.pipeline.step.annotation.step;

import com.github.loki4j.slf4j.marker.LabelMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithLogMarker
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithLogMarker.class);

    @StepConfig(id = "step-with_log-marker")
    public Result execute(LogMarker marker)
    {
        logger.info("log-marker: {}", marker);
        LabelMarker markers = marker.mark();
        return new TestResult("annotation-test", markers.getLabels().get("step"));
    }
}
