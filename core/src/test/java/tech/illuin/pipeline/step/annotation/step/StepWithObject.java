package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.PipelineStepAnnotationTest.TestObject;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithObject
{
    private static final Logger logger = LoggerFactory.getLogger(StepWithObject.class);

    @StepConfig(id = "step-with_object")
    public Result execute(@Object TestObject object)
    {
        logger.info("object: {}", object);
        return new TestResult("annotation-test", Objects.toString(object.uid()));
    }
}
