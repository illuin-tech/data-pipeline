package tech.illuin.pipeline.step.execution.wrapper.timelimiter;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>
{
    private final TimeLimiter limiter;

    public TimeLimiterWrapper(TimeLimiterConfig config)
    {
        this.limiter = TimeLimiterRegistry.of(config).timeLimiter("time-limiter-" + Indexable.generateUid());
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new TimeLimiterStep<>(step, this.limiter);
    }
}
