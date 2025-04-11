package tech.illuin.pipeline.generic.wrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterSinkHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class TestTimeLimiterSinkHandler implements TimeLimiterSinkHandler
{
    protected final String name;
    private final AtomicInteger onSuccessCounter;
    private final AtomicInteger onErrorCounter;

    private static final Logger logger = LoggerFactory.getLogger(TestTimeLimiterSinkHandler.class);

    public TestTimeLimiterSinkHandler(String name) {
        this.name = name;
        this.onSuccessCounter = new AtomicInteger(0);
        this.onErrorCounter = new AtomicInteger(0);
    }

    @Override
    public void onError(Output output, Context context, Exception e)
    {
        Integer count = this.onErrorCounter.incrementAndGet();
        logger.info("{} onError count: {}", this.name, count);
    }

    @Override
    public void onSuccess(Output output, Context context)
    {
        Integer count = this.onSuccessCounter.incrementAndGet();
        logger.info("{} onSuccess count: {}", this.name, count);
    }

    public AtomicInteger onSuccessCounter() {
        return onSuccessCounter;
    }

    public AtomicInteger onErrorCounter() {
        return onErrorCounter;
    }
}
