package tech.illuin.pipeline.execution.wrapper;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.sink.execution.wrapper.timelimiter.TimeLimiterSink;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.execution.wrapper.timelimiter.TimeLimiterStep;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>, SinkWrapper<P>
{
    private final TimeLimiter limiter;
    private final Executor executor;

    public TimeLimiterWrapper(TimeLimiterConfig config)
    {
        this(config, ForkJoinPool.commonPool());
    }

    public TimeLimiterWrapper(TimeLimiterConfig config, Executor executor)
    {
        this.limiter = TimeLimiterRegistry.of(config).timeLimiter("time-limiter-" + KSUIDGenerator.INSTANCE.generate());
        this.executor = executor;
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new TimeLimiterStep<>(step, this.limiter, this.executor);
    }

    @Override
    public Sink<P> wrap(Sink<P> sink)
    {
        return new TimeLimiterSink<>(sink, this.limiter, this.executor);
    }
}
