package tech.illuin.pipeline.generic.wrapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterStepHandler;
import tech.illuin.pipeline.step.result.ResultView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class TestTimeLimiterStepHandler implements TimeLimiterStepHandler
{
    protected final String name;
    private final AtomicInteger onSuccessCounter;
    private final AtomicInteger onErrorCounter;

    private static final Logger logger = LoggerFactory.getLogger(TestTimeLimiterStepHandler.class);

    public TestTimeLimiterStepHandler(String name)
    {
        this.name = name;
        this.onSuccessCounter = new AtomicInteger(0);
        this.onErrorCounter = new AtomicInteger(0);
    }

    @Override
    public <T extends Indexable, I> void onError(T object, I input, Object payload, ResultView view, Context context, Exception e)
    {
        Integer count = onErrorCounter.incrementAndGet();
        logger.info("{} onError count: {}", this.name, count);
    }

    @Override
    public <T extends Indexable, I> void onSuccess(T object, I input, Object payload, ResultView view, Context context)
    {
        Integer count = onSuccessCounter.incrementAndGet();
        logger.info("{} onSuccess count: {}", this.name, count);
    }

    public AtomicInteger onSuccessCounter() {
        return onSuccessCounter;
    }

    public AtomicInteger onErrorCounter() {
        return onErrorCounter;
    }
}
