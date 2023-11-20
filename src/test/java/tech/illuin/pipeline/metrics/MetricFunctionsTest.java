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

    @Test
    public void testCombineMarkersTriple__shouldSucceed()
    {
        Map<String, String> mainstay = Map.of("a", "first", "b", "second");
        Map<String, String> globalDynamic = Map.of("c", "third");
        Map<String, String> localDynamic = Map.of("d", "fourth");

        Map<String, String> combined = Assertions.assertDoesNotThrow(() -> MetricFunctions.combine(mainstay, globalDynamic, localDynamic));

        Assertions.assertEquals(4, combined.size());
        Assertions.assertTrue(combined.containsKey("c"));
        Assertions.assertEquals("third", combined.get("c"));
        Assertions.assertTrue(combined.containsKey("d"));
        Assertions.assertEquals("fourth", combined.get("d"));
    }

    @Test
    public void testCombineMarkersTriple__overrideGlobalWithLocal_shouldSucceed()
    {
        Map<String, String> mainstay = Map.of("a", "first", "b", "second");
        Map<String, String> globalDynamic = Map.of("c", "third");
        Map<String, String> localDynamic = Map.of("c", "override");

        Map<String, String> combined = Assertions.assertDoesNotThrow(() -> MetricFunctions.combine(mainstay, globalDynamic, localDynamic));

        Assertions.assertEquals(3, combined.size());
        Assertions.assertTrue(combined.containsKey("c"));
        Assertions.assertEquals("override", combined.get("c"));
    }

    @Test
    public void testCombineMarkersTriple__overrideMainstayWithGlobal_shouldFail()
    {
        Map<String, String> mainstay = Map.of("a", "first", "b", "second");
        Map<String, String> globalDynamic = Map.of("b", "override");
        Map<String, String> localDynamic = Map.of("c", "third");

        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricFunctions.combine(mainstay, globalDynamic, localDynamic));
    }

    @Test
    public void testCombineMarkersTriple__overrideMainstayWithLocal_shouldFail()
    {
        Map<String, String> mainstay = Map.of("a", "first", "b", "second");
        Map<String, String> globalDynamic = Map.of("c", "third");
        Map<String, String> localDynamic = Map.of("b", "override");

        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricFunctions.combine(mainstay, globalDynamic, localDynamic));
    }

    @Test
    public void testCombineMarkersTriple__overrideGlobalWithGlobalAndLocal_shouldFail()
    {
        Map<String, String> mainstay = Map.of("a", "first", "b", "second");
        Map<String, String> globalDynamic = Map.of("b", "override-global");
        Map<String, String> localDynamic = Map.of("b", "override-local");

        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricFunctions.combine(mainstay, globalDynamic, localDynamic));
    }
}
