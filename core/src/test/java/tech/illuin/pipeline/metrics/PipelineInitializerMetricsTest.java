package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;
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
    public void testMDC()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineInitializationMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineInitializationMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mdc", "test-mdc-initializer"),
            new MetricTags().put("test", "true"),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC();
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("initializer"));
        Assertions.assertEquals("test-mdc-initializer", ctx0.get("initializer"));

        metrics.unsetMDC();
        Map<String, String> ctx1 = adapter.getCopyOfContextMap();

        Assertions.assertFalse(ctx1.containsKey("initializer"));
    }

    @Test
    public void testMDCException()
    {
        MDCAdapter adapter = new BasicMDCAdapter();
        PipelineInitializationMetrics metrics = Assertions.assertDoesNotThrow(() -> new PipelineInitializationMetrics(
            new SimpleMeterRegistry(),
            createTag("test-mdc-exception", "test-mdc-initializer-exception"),
            new MetricTags().put("test", "true"),
            new DebugMDCManager(adapter)
        ));

        metrics.setMDC(new Exception());
        Map<String, String> ctx0 = adapter.getCopyOfContextMap();

        Assertions.assertTrue(ctx0.containsKey("error"));
        Assertions.assertEquals("java.lang.Exception", ctx0.get("error"));

        metrics.unsetMDC();
        Map<String, String> ctx1 = adapter.getCopyOfContextMap();

        Assertions.assertFalse(ctx1.containsKey("initializer"));
        Assertions.assertFalse(ctx1.containsKey("error"));
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
