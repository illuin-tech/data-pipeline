package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.metering.tag.MetricScope;
import tech.illuin.pipeline.metering.tag.MetricTags;

import java.util.Collection;
import java.util.Map;

public class MetricTagsTest
{
    @Test
    public void test__empty()
    {
        var metricTags = new MetricTags();

        Map<String, String> map = Assertions.assertDoesNotThrow(metricTags::asMap);
        Assertions.assertTrue(map.isEmpty());

        Collection<Tag> tags = Assertions.assertDoesNotThrow(metricTags::asTags);
        Assertions.assertTrue(tags.isEmpty());

        Map<String, String> markers = Assertions.assertDoesNotThrow(metricTags::asMarker);
        Assertions.assertTrue(markers.isEmpty());
    }

    @Test
    public void test__scoped()
    {
        var metricTags = new MetricTags();
        metricTags.put("a", "anything-goes");
        metricTags.put("b", "high-cardinality", MetricScope.MARKER);
        metricTags.put("c", "low-cardinality", MetricScope.TAG);

        Assertions.assertTrue(metricTags.has("a", MetricScope.MARKER));
        Assertions.assertTrue(metricTags.has("a", MetricScope.TAG));
        Assertions.assertEquals("anything-goes", metricTags.get("a", MetricScope.MARKER).orElse(null));
        Assertions.assertEquals("anything-goes", metricTags.get("a", MetricScope.TAG).orElse(null));

        Assertions.assertTrue(metricTags.has("b", MetricScope.MARKER));
        Assertions.assertFalse(metricTags.has("b", MetricScope.TAG));
        Assertions.assertEquals("high-cardinality", metricTags.get("b", MetricScope.MARKER).orElse(null));
        Assertions.assertTrue(metricTags.get("b", MetricScope.TAG).isEmpty());

        Assertions.assertFalse(metricTags.has("c", MetricScope.MARKER));
        Assertions.assertTrue(metricTags.has("c", MetricScope.TAG));
        Assertions.assertTrue(metricTags.get("c", MetricScope.MARKER).isEmpty());
        Assertions.assertEquals("low-cardinality", metricTags.get("c", MetricScope.TAG).orElse(null));

        Map<String, String> map = Assertions.assertDoesNotThrow(metricTags::asMap);
        Assertions.assertEquals(3, map.size());

        Collection<Tag> tags = Assertions.assertDoesNotThrow(metricTags::asTags);
        Assertions.assertEquals(2, tags.size());

        Map<String, String> markers = Assertions.assertDoesNotThrow(metricTags::asMarker);
        Assertions.assertEquals(2, markers.size());
    }
}
