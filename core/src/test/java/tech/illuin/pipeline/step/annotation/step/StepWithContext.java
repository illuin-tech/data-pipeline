package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWithContext
{
    private final String key;

    private static final Logger logger = LoggerFactory.getLogger(StepWithContext.class);

    public StepWithContext(String key)
    {
        this.key = key;
    }

    @StepConfig(id = "step-with_context")
    public Result execute(Context context)
    {
        logger.info("context: {}", context);
        return new TestResult("annotation-test", context.get(this.key, String.class).orElse(null));
    }
}
