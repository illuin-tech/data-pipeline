package tech.illuin.pipeline.resilience4j.execution.wrapper;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.resilience4j.sink.wrapper.retry.RetrySink;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.resilience4j.step.wrapper.retry.RetryStep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryWrapper<T extends Indexable, I> implements StepWrapper<T, I>, SinkWrapper
{
    private final Retry retry;

    public RetryWrapper(RetryConfig config)
    {
        this.retry = RetryRegistry.of(config).retry("retry-" + KSUIDGenerator.INSTANCE.generate());
    }

    @Override
    public Step<T, I> wrap(Step<T, I> step)
    {
        return new RetryStep<>(step, this.retry);
    }

    @Override
    public Sink wrap(Sink sink)
    {
        return new RetrySink(sink, this.retry);
    }
}
