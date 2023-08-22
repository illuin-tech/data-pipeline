package tech.illuin.pipeline.sink.annotation.sink;

import com.github.loki4j.slf4j.marker.LabelMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithLogMarker
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithLogMarker.class);

    public SinkWithLogMarker(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_log-marker")
    public void execute(LogMarker marker)
    {
        logger.info("log-marker: {}", marker);
        LabelMarker markers = marker.mark();
        this.collector.update(markers.getLabels().get("sink"));
    }
}
