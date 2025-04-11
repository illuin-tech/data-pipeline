package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import tech.illuin.pipeline.input.uid_generator.TSIDGenerator;
import tech.illuin.pipeline.metering.PipelineMarkerManager;
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
            new PipelineMarkerManager(
                createTag("test-create"),
                new MetricTags().put("test", "true")
            )
        ));
    }

    @Test
    public void testMDC()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            new PipelineMarkerManager(
                createTag("test-mdc"),
                new MetricTags().put("test", "true")
            ),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC();
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("pipeline"));
        Assertions.assertTrue(ctx0.containsKey("author"));
        Assertions.assertTrue(ctx0.containsKey("test"));
        Assertions.assertEquals("test-mdc", ctx0.get("pipeline"));
        Assertions.assertEquals(ANONYMOUS, ctx0.get("author"));
        Assertions.assertEquals("true", ctx0.get("test"));

        metrics.unsetMDC();
        Map<String, String> ctx1 = adapter.getCopyOfContextMap();

        Assertions.assertFalse(ctx1.containsKey("pipeline"));
        Assertions.assertFalse(ctx1.containsKey("author"));
        Assertions.assertFalse(ctx1.containsKey("test"));
    }

    @Test
    public void testMDC_shouldHandleNulls()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            new PipelineMarkerManager(
                new PipelineTag(TSIDGenerator.INSTANCE.generate(), "test-mdc-nullable", null),
                new MetricTags().put("test", "true")
            ),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC();
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("pipeline"));
        Assertions.assertTrue(ctx0.containsKey("author"));
        Assertions.assertTrue(ctx0.containsKey("test"));
        Assertions.assertEquals("test-mdc-nullable", ctx0.get("pipeline"));
        Assertions.assertNull(ctx0.get("author"));
        Assertions.assertEquals("true", ctx0.get("test"));
    }

    @Test
    public void testMDCException()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineMetrics(
            new SimpleMeterRegistry(),
            new PipelineMarkerManager(
                createTag("test-mark-exception"),
                new MetricTags().put("test", "true")
            ),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC(new Exception());
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("error"));
        Assertions.assertEquals("java.lang.Exception", ctx0.get("error"));

        metrics.unsetMDC();
        Map<String, String> ctx1 = adapter.getCopyOfContextMap();

        Assertions.assertFalse(ctx1.containsKey("error"));
    }

    private static PipelineTag createTag(String pipeline)
    {
        return new PipelineTag(TSIDGenerator.INSTANCE.generate(), pipeline, ANONYMOUS);
    }
}
