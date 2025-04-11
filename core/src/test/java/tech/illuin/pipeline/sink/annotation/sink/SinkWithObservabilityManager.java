package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithObservabilityManager
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithObservabilityManager.class);

    public SinkWithObservabilityManager(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_observability-manager")
    public void execute(ObservabilityManager manager)
    {
        logger.info("observability-manager: meterRegistry={} tracer={}", manager.meterRegistry().getClass().getName(), manager.tracer().getClass().getName());
        this.collector.update(manager.meterRegistry().getClass().getSimpleName());
    }
}
