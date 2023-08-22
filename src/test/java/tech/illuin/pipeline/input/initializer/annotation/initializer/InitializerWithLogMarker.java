package tech.illuin.pipeline.input.initializer.annotation.initializer;

import com.github.loki4j.slf4j.marker.LabelMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestObject;
import tech.illuin.pipeline.input.initializer.annotation.PipelineInitializerAnnotationTest.TestPayload;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.marker.LogMarker;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerWithLogMarker
{
    private static final Logger logger = LoggerFactory.getLogger(InitializerWithLogMarker.class);

    @InitializerConfig(id = "init-with_log-marker")
    public TestPayload execute(LogMarker marker, UIDGenerator generator)
    {
        logger.info("log-marker: {}", marker);
        LabelMarker markers = marker.mark();
        return new TestPayload(new TestObject(generator.generate(), markers.getLabels().get("initializer")));
    }
}
