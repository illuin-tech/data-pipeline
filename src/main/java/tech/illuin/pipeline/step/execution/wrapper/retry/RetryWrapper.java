package tech.illuin.pipeline.step.execution.wrapper.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>
{
    private final Retry retry;

    public RetryWrapper(RetryConfig config)
    {
        this.retry = RetryRegistry.of(config).retry("retry-" + Indexable.generateUid());
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new RetryStep<>(step, this.retry);
    }
}
