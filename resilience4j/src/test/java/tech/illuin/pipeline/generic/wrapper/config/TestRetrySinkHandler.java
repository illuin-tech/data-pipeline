package tech.illuin.pipeline.generic.wrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetrySinkHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class TestRetrySinkHandler extends TestTimeLimiterSinkHandler implements RetrySinkHandler
{
    private final AtomicInteger onRetryCounter;

    private static final Logger logger = LoggerFactory.getLogger(TestRetrySinkHandler.class);

    public TestRetrySinkHandler(String name) {
        super(name);
        this.onRetryCounter = new AtomicInteger(0);
    }

    @Override
    public void onRetry(Output output, Context context) {
        Integer count = this.onRetryCounter.incrementAndGet();
        logger.info("{} onRetry count: {}", this.name, count);
    }

    public AtomicInteger onRetryCounter() {
        return onRetryCounter;
    }
}
