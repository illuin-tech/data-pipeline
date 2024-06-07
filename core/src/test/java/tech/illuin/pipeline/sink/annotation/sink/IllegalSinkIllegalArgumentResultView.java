package tech.illuin.pipeline.sink.annotation.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalSinkIllegalArgumentResultView
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalSinkIllegalArgumentResultView.class);

    @SinkConfig
    public void execute(ResultView resultView)
    {
        logger.info("result_view: {}", resultView);
    }
}
