package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithMarkerManager
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithMarkerManager.class);

    public SinkWithMarkerManager(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_marker-manager")
    public void execute(MarkerManager marker)
    {
        logger.info("markers={} tags={} discriminants={}", marker.markers(), marker.tags(), marker.discriminants());
        this.collector.update(marker.getClass().getSimpleName());
    }
}
