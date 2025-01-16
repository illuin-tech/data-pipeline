package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;
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
    public void testMDC()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineSinkMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mdc", "test-mdc-sink"),
            new MetricTags().put("test", "true"),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC();
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("sink"));
        Assertions.assertEquals("test-mdc-sink", ctx0.get("sink"));

        metrics.unsetMDC();
        Map<String, String> ctx1 = adapter.getCopyOfContextMap();

        Assertions.assertFalse(ctx1.containsKey("sink"));
    }

    @Test
    public void testMDCException()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineSinkMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineSinkMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mdc-exception", "test-mdc-sink-exception"),
            new MetricTags().put("test", "true"),
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
