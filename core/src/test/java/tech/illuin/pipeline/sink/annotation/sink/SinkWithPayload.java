package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Payload;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.TestPayload;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithPayload
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithPayload.class);

    public SinkWithPayload(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_payload")
    public void execute(@Payload TestPayload payload)
    {
        logger.info("payload: {}", payload);
        this.collector.update(Objects.toString(payload.object().uid()));
    }
}
