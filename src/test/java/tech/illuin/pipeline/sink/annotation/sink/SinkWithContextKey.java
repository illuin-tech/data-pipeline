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

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithContextKey
{
    private final List<String> collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithContextKey.class);

    public SinkWithContextKey(List<String> collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_context-key")
    public void execute(@Context("some-metadata") String metadata)
    {
        logger.info("context-key: {}", metadata);
        this.collector.add("single(" + metadata + ")");
    }
}
