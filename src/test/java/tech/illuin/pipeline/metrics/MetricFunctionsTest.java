package tech.illuin.pipeline.metrics;

import io.micrometer.core.instrument.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.metering.MetricFunctions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MetricFunctionsTest
{
    @Test
    public void testCombineTags__shouldSucceed()
    {
        Collection<Tag> listA = List.of(Tag.of("a", "first"), Tag.of("b", "second"));
        Collection<Tag> listB = List.of(Tag.of("c", "third"));

        Collection<Tag> combined = Assertions.assertDoesNotThrow(() -> MetricFunctions.combine(listA, listB));

        Assertions.assertEquals(3, combined.size());
        Assertions.assertEquals("c", combined.toArray(new Tag[3])[2].getKey());
    }

    @Test
    public void testCombineTags__shouldFail()
    {
        Collection<Tag> listA = List.of(Tag.of("a", "first"), Tag.of("b", "second"));
        Collection<Tag> listB = List.of(Tag.of("b", "override"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricFunctions.combine(listA, listB));
    }

    @Test
    public void testCombineMarkers__shouldSucceed()
    {
        Map<String, String> mapA = Map.of("a", "first", "b", "second");
        Map<String, String> mapB = Map.of("c", "third");

        Map<String, String> combined = Assertions.assertDoesNotThrow(() -> MetricFunctions.combine(mapA, mapB));

        Assertions.assertEquals(3, combined.size());
        Assertions.assertTrue(combined.containsKey("c"));
        Assertions.assertEquals("third", combined.get("c"));
    }

    @Test
    public void testCombineMarkers__shouldFail()
    {
        Map<String, String> mapA = Map.of("a", "first", "b", "second");
        Map<String, String> mapB = Map.of("b", "override");

        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricFunctions.combine(mapA, mapB));
    }
}
