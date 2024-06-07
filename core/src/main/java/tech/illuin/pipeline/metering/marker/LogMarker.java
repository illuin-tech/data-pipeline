package tech.illuin.pipeline.metering.marker;

import com.github.loki4j.slf4j.marker.LabelMarker;

import java.util.Collections;
import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface LogMarker
{
    default LabelMarker mark()
    {
        return this.mark(Collections.emptyMap());
    }

    default LabelMarker mark(String key, String label)
    {
        return this.mark(Collections.singletonMap(key, label));
    }

    LabelMarker mark(Map<String, String> labels);

    LabelMarker mark(Exception exception);
}
