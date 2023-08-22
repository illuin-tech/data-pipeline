package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.uid_generator.TSIDGenerator;
import tech.illuin.pipeline.metering.PipelineInitializationMetrics;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Map;

import static tech.illuin.pipeline.input.author_resolver.AuthorResolver.ANONYMOUS;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializerMetricsTest
{
    @Test
    public void testCreate()
    {
        Assertions.assertDoesNotThrow(() -> new PipelineInitializationMetrics(
            new SimpleMeterRegistry(),
            createTag("test-create", "test-create-initializer"),
            new MetricTags().put("test", "true")
        ));
    }

    @Test
    public void testMark()
    {
        PipelineInitializationMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineInitializationMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark", "test-mark-initializer"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark().getLabels();

        Assertions.assertEquals("test-mark", labels.get("pipeline"));
        Assertions.assertEquals("test-mark-initializer", labels.get("initializer"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("true", labels.get("test"));
    }

    @Test
    public void testMarkException()
    {
        PipelineInitializationMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineInitializationMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mark-exception", "test-mark-initializer-exception"),
            new MetricTags().put("test", "true")
        ));

        Map<String, String> labels = metrics.mark(new Exception()).getLabels();

        Assertions.assertEquals("test-mark-exception", labels.get("pipeline"));
        Assertions.assertEquals("test-mark-initializer-exception", labels.get("initializer"));
        Assertions.assertEquals(ANONYMOUS, labels.get("author"));
        Assertions.assertEquals("java.lang.Exception", labels.get("error"));
        Assertions.assertEquals("true", labels.get("test"));
    }
    
    private static ComponentTag createTag(String pipeline, String initializer)
    {
        return new ComponentTag(
            TSIDGenerator.INSTANCE.generate(),
            new PipelineTag(TSIDGenerator.INSTANCE.generate(), pipeline, ANONYMOUS),
            initializer,
            ComponentFamily.INITIALIZER
        );
    }
}
