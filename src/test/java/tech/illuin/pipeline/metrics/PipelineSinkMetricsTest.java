package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.uid_generator.TSIDGenerator;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Map;

import static tech.illuin.pipeline.input.author_resolver.AuthorResolver.ANONYMOUS;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkMetricsTest
{
    @Test
    public void testCreate()
    {
        Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-create", "test-create-sink"),
            new MetricTags().put("test", "true")
        ));
    }

    @Test
    public void testMark()
    {
        PipelineSinkMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark", "test-mark-sink"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark().getLabels();

        Assertions.assertEquals("test-mark", labels.get("pipeline"));
        Assertions.assertEquals("test-mark-sink", labels.get("sink"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("true", labels.get("test"));
    }

    @Test
    public void testMarkDynamic()
    {
        PipelineSinkMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark", "test-mark-sink"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark("dynamic", "true").getLabels();

        Assertions.assertEquals("test-mark", labels.get("pipeline"));
        Assertions.assertEquals("test-mark-sink", labels.get("sink"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("true", labels.get("test"));
        Assertions.assertEquals("true", labels.get("dynamic"));
    }

    @Test
    public void testMarkException()
    {
        PipelineSinkMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark-exception", "test-mark-sink-exception"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark(new Exception()).getLabels();

        Assertions.assertEquals("test-mark-exception", labels.get("pipeline"));
        Assertions.assertEquals("test-mark-sink-exception", labels.get("sink"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("java.lang.Exception", labels.get("error"));
        Assertions.assertEquals("true", labels.get("test"));
    }
    
    private static ComponentTag createTag(String pipeline, String sink)
    {
        return new ComponentTag(
            TSIDGenerator.INSTANCE.generate(),
            new PipelineTag(TSIDGenerator.INSTANCE.generate(), pipeline, ANONYMOUS),
            sink,
            ComponentFamily.SINK
        );
    }
}
