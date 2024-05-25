package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalStepIllegalArgumentOutput
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalStepIllegalArgumentOutput.class);

    @StepConfig
    public Result execute(Output output)
    {
        logger.info("output: {}", output);
        return new TestResult("test-annotation", Objects.toString(output));
    }
}
