package tech.illuin.pipeline.generic.pipeline.sink.execution.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class WrapAll implements SinkErrorHandler
{
    private static final Logger logger = LoggerFactory.getLogger(WrapAll.class);

    @Override
    public void handle(Exception exception, Context<?> context)
    {
        logger.error(exception.getMessage());
    }
}
