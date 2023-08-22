package tech.illuin.pipeline.metering.marker;

import com.github.loki4j.slf4j.marker.LabelMarker;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface LogMarker
{
    LabelMarker mark();

    LabelMarker mark(Exception exception);
}
