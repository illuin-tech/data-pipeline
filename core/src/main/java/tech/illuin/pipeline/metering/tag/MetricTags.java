package tech.illuin.pipeline.metering.tag;

import io.micrometer.core.instrument.Tag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.illuin.pipeline.metering.tag.MetricScope.MARKER;
import static tech.illuin.pipeline.metering.tag.MetricScope.TAG;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class MetricTags
{
    private final Map<String, ScopedTag> data;

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
        return Optional.ofNullable(this.data.get(key)).map(ScopedTag::value);
    }

    public <E extends Enum<E>> Optional<String> get(E key)
    {
        return this.get(key.name());
    }

    public MetricTags put(String key, String value)
    {
        this.put(key, value, MetricScope.values());
        return this;
    }

    public MetricTags put(String key, String value, MetricScope... scopes)
    {
        this.data.put(key, new ScopedTag(value, Set.of(scopes)));
        return this;
    }

    public <E extends Enum<E>> MetricTags put(E key, String value)
    {
        return this.put(key.name(), value);
    }

    public Map<String, String> asMap()
    {
        return this.data.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> e.getValue().value()
            ));
    }

    public Map<String, String> asMarker()
    {
        return this.filterData(MARKER)
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> e.getValue().value()
            ));
    }

    public Collection<Tag> asTags()
    {
        return this.filterData(TAG)
            .map(e -> Tag.of(e.getKey(), e.getValue().value() == null ? "" : e.getValue().value()))
            .toList();
    }

    private Stream<Map.Entry<String, ScopedTag>> filterData(MetricScope scope)
    {
        return this.data.entrySet().stream()
            .filter(e -> e.getValue().scopes().contains(scope));
    }

    private record ScopedTag(
        String value,
        Set<MetricScope> scopes
    ) {}
}
