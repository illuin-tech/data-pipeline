package tech.illuin.pipeline.generic.wrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetryStepHandler;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class TestRetryStepHandler extends TestTimeLimiterStepHandler implements RetryStepHandler
{
    private final AtomicInteger onRetryCounter;

    private static final Logger logger = LoggerFactory.getLogger(TestRetryStepHandler.class);

    public TestRetryStepHandler(String name)
    {
        super(name);
        this.onRetryCounter = new AtomicInteger(0);
    }

    @Override
    public <T extends Indexable, I> void onRetry(T object, I input, Object payload, ResultView view, Context context) {
        Integer count = this.onRetryCounter.incrementAndGet();
        logger.info("{} onRetry count: {}", this.name, count);
    }

    public AtomicInteger onRetryCounter() {
        return onRetryCounter;
    }
}
