package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithContext
{
    private final String key;
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithContext.class);

    public SinkWithContext(String key, StringCollector collector)
    {
        this.key = key;
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_context")
    public void execute(Context<?> context)
    {
        logger.info("context: {}", context);
        this.collector.update(context.get(this.key, String.class).orElse(null));
    }
}
