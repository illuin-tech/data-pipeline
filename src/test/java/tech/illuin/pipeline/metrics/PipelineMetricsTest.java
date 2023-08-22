package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.uid_generator.TSIDGenerator;
import tech.illuin.pipeline.metering.PipelineMetrics;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Map;

import static tech.illuin.pipeline.input.author_resolver.AuthorResolver.ANONYMOUS;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineMetricsTest
{
    @Test
    public void testCreate()
    {
        Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            createTag("test-create"),
            new MetricTags().put("test", "true")
        ));
    }

    @Test
    public void testMark()
    {
        PipelineMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark().getLabels();

        Assertions.assertEquals("test-mark", labels.get("pipeline"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("true", labels.get("test"));
    }

    @Test
    public void testMarkException()
    {
        PipelineMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark-exception"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark(new Exception()).getLabels();

        Assertions.assertEquals("test-mark-exception", labels.get("pipeline"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("java.lang.Exception", labels.get("error"));
        Assertions.assertEquals("true", labels.get("test"));
    }

    private static PipelineTag createTag(String pipeline)
    {
        return new PipelineTag(TSIDGenerator.INSTANCE.generate(), pipeline, ANONYMOUS);
    }
}
