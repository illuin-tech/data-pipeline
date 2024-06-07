package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithContextKeyPrimitive
{
    private final List<String> collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithContextKey.class);

    public SinkWithContextKeyPrimitive(List<String> collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_context-key-primitive")
    public void execute(@Context("some-primitive") int metadata)
    {
        logger.info("context-key: {}", metadata);
        this.collector.add("primitive(" + metadata + ")");
    }
}
