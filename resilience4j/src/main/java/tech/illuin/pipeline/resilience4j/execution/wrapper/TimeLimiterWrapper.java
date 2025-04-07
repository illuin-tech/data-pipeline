package tech.illuin.pipeline.resilience4j.execution.wrapper;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterWrapperConfig;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.resilience4j.sink.wrapper.timelimiter.TimeLimiterSink;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.resilience4j.step.wrapper.timelimiter.TimeLimiterStep;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TimeLimiterWrapper<T extends Indexable, I> implements StepWrapper<T, I>, SinkWrapper
{
    private final TimeLimiter limiter;
    private final ExecutorService executor;
    private final TimeLimiterWrapperConfig wrapperConfig;

    public TimeLimiterWrapper(TimeLimiterConfig config, TimeLimiterWrapperConfig wrapperConfig)
    {
        this(config, ForkJoinPool.commonPool(), wrapperConfig);
    }

    public TimeLimiterWrapper(TimeLimiterConfig config)
    {
        this(config, TimeLimiterWrapperConfig.NOOP_WRAPPER_CONFIG);
    }

    public TimeLimiterWrapper(TimeLimiterConfig config, ExecutorService executor, TimeLimiterWrapperConfig wrapperConfig)
    {
        this.limiter = TimeLimiterRegistry.of(config).timeLimiter("time-limiter-" + KSUIDGenerator.INSTANCE.generate());
        this.executor = executor;
        this.wrapperConfig = wrapperConfig;
    }

    public TimeLimiterWrapper(TimeLimiterConfig config, ExecutorService executor)
    {
        this(config, executor, TimeLimiterWrapperConfig.NOOP_WRAPPER_CONFIG);
    }

    @Override
    public Step<T, I> wrap(Step<T, I> step)
    {
        return new TimeLimiterStep<>(step, this.limiter, this.executor, this.wrapperConfig.stepHandler());
    }

    @Override
    public Sink wrap(Sink sink)
    {
        return new TimeLimiterSink(sink, this.limiter, this.executor, this.wrapperConfig.sinkHandler());
    }
}
