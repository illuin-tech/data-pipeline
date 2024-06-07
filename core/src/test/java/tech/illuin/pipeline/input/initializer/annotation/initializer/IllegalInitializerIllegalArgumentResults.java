package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalInitializerIllegalArgumentResults
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalInitializerIllegalArgumentResults.class);

    @InitializerConfig
    public TestPayload execute(Results results, UIDGenerator generator)
    {
        logger.info("results: {}", results);
        return new TestPayload(new TestObject(generator.generate(), "illegal_results"));
    }
}
