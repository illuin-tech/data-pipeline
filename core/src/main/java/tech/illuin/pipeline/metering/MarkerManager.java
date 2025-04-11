package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Tag;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MarkerManager
{
    Set<Tag> discriminants();

    Set<Tag> tags();

    default Set<Tag> tags(Tag... tags)
    {
        return Stream.concat(
            this.tags().stream(),
            Stream.of(tags)
        ).collect(Collectors.toSet());
    }

    Map<String, String> markers();
}
