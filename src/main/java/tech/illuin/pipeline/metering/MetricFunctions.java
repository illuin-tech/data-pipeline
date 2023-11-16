package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Tag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class MetricFunctions
{
    private MetricFunctions() {}

    /**
     * Combines a generated collection and a mainstay collection of metric tags, the mainstay collection entries are reserved entries and cannot be overwritten.
     *
     * @param mainstayTags the reserved tags
     * @param tags the generated tags
     * @return the resulting tag collection
     */
    public static Collection<Tag> combine(Collection<Tag> mainstayTags, Collection<Tag> tags)
    {
        List<Tag> combined = new ArrayList<>(mainstayTags);
        Set<String> reservedKeys = mainstayTags.stream().map(Tag::getKey).collect(Collectors.toSet());
        for (Tag tag : tags)
        {
            if (reservedKeys.contains(tag.getKey()))
                throw new IllegalArgumentException("Generated tags contain the reserved mainstay key " + tag.getKey());
            combined.add(tag);
        }
        return combined;
    }

    /**
     * @see #combine(Map, Map, Map)
     */
    public static Map<String, String> combine(Map<String, String> mainstayMarkers, Map<String, String> markers)
    {
        return combine(mainstayMarkers, markers, Collections.emptyMap());
    }

    /**
     * Combines a generated map and a mainstay map of log markers, the mainstay map entries are reserved entries and cannot be overwritten.
     *
     * @param mainstayMarkers the reserved markers
     * @param markers the generated markers
     * @param dynamicMarkers dynamically provided markers
     * @return the resulting marker map
     */
    public static Map<String, String> combine(Map<String, String> mainstayMarkers, Map<String, String> markers, Map<String, String> dynamicMarkers)
    {
        Map<String, String> combined = new HashMap<>(mainstayMarkers);

        combineInto(combined, mainstayMarkers, markers);
        combineInto(combined, mainstayMarkers, dynamicMarkers);

        return combined;
    }

    private static void combineInto(Map<String, String> combined, Map<String, String> mainstayMarkers, Map<String, String> markers)
    {
        for (Map.Entry<String, String> entry : markers.entrySet())
        {
            if (mainstayMarkers.containsKey(entry.getKey()))
                throw new IllegalArgumentException("Supplied dynamic markers contain the reserved mainstay key " + entry.getKey());
            combined.put(entry.getKey(), entry.getValue());
        }
    }
}
