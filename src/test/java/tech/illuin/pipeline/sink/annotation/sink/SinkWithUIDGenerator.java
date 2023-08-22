package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithUIDGenerator
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithUIDGenerator.class);

    public SinkWithUIDGenerator(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_uid-generator")
    public void execute(UIDGenerator generator)
    {
        logger.info("uid-generator: {}", generator);
        this.collector.update(generator.getClass().getSimpleName());
    }
}
