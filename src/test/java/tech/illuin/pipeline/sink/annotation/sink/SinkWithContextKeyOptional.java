package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;
import java.util.Optional;

import static tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithContextKeyOptional
{
    private final List<String> collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithContextKey.class);

    public SinkWithContextKeyOptional(List<String> collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_context-key-optional")
    public void execute(@Context("some-metadata") Optional<String> metadata)
    {
        String value = metadata.orElse(null);
        logger.info("context-key: {}", value);
        this.collector.add("optional(" + value + ")");
    }
}
