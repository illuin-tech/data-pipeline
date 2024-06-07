package tech.illuin.pipeline.input.initializer.annotation.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalInitializerIllegalArgumentResultView
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalInitializerIllegalArgumentResultView.class);

    @InitializerConfig
    public TestPayload execute(ResultView resultView, UIDGenerator generator)
    {
        logger.info("result_view: {}", resultView);
        return new TestPayload(new TestObject(generator.generate(), "illegal_result_view"));
    }
}
