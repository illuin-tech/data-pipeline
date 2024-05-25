package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalInitializerIllegalArgumentOutput
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalInitializerIllegalArgumentOutput.class);

    @InitializerConfig
    public TestPayload execute(Output output, UIDGenerator generator)
    {
        logger.info("output: {}", output);
        return new TestPayload(new TestObject(generator.generate(), "illegal_output"));
    }
}
