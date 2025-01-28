package tech.illuin.pipeline.metering.tag;

import io.micrometer.core.instrument.Tag;

import java.util.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class MetricTags
{
    private final Map<String, String> data;

    public MetricTags()
    {
        this.data = new HashMap<>();
    }

    public boolean has(String key)
    {
        return this.data.containsKey(key);
    }

    public <E extends Enum<E>> boolean has(E key)
    {
        return this.has(key.name());
    }

    public Optional<String> get(String key)
    {
        return Optional.ofNullable(this.data.get(key));
    }

    public <E extends Enum<E>> Optional<String> get(E key)
    {
        return this.get(key.name());
    }

    public MetricTags put(String key, String value)
    {
        this.data.put(key, value);
        return this;
    }

    public <E extends Enum<E>> MetricTags put(E key, String value)
    {
        return this.put(key.name(), value);
    }

    public Map<String, String> asMap()
    {
        return Collections.unmodifiableMap(this.data);
    }

    public Collection<Tag> asTags()
    {
        return this.data.entrySet().stream()
            .map(e -> Tag.of(e.getKey(), e.getValue() == null ? "" : e.getValue()))
            .toList();
    }
}
