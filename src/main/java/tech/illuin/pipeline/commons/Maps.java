package tech.illuin.pipeline.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Maps
{
    private Maps() {}

    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2)
    {
        return merge(map1, map2, HashMap::new);
    }

    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2, Supplier<Map<K, V>> initializer)
    {
        Map<K, V> merged = initializer.get();
        merged.putAll(map1);
        merged.putAll(map2);
        return merged;
    }
}
