package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Payload;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.PipelineStepAnnotationTest.TestPayload;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithPayload
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithPayload.class);

    @StepConfig(id = "step-with_payload")
    public Result execute(@Payload TestPayload payload)
    {
        logger.info("payload: {}", payload);
        return new TestResult("annotation-test", Objects.toString(payload.object().uid()));
    }
}
