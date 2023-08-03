package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.TestPayload;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.Objects;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithOutput
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithOutput.class);

    public SinkWithOutput(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_output")
    public void execute(Output<TestPayload> output)
    {
        logger.info("output: {}", output);
        this.collector.update(Objects.toString(output.payload().object().uid()));
    }
}
