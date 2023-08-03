package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.sink.annotation.PipelineSinkAnnotationTest.StringCollector;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWithResults
{
    private final StringCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SinkWithResults.class);

    public SinkWithResults(StringCollector collector)
    {
        this.collector = collector;
    }

    @SinkConfig(id = "sink-with_results")
    public void execute(Results results)
    {
        logger.info("results: {}", results);
        this.collector.update(results.latest(TestResult.class).map(TestResult::status).orElse(null));
    }
}
