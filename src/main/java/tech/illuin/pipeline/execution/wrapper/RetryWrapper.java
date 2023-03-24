package tech.illuin.pipeline.execution.wrapper;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.sink.execution.wrapper.retry.RetrySink;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.execution.wrapper.retry.RetryStep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class RetryWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>, SinkWrapper<P>
{
    private final Retry retry;

    public RetryWrapper(RetryConfig config)
    {
        this.retry = RetryRegistry.of(config).retry("retry-" + KSUIDGenerator.INSTANCE.generate());
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new RetryStep<>(step, this.retry);
    }

    @Override
    public Sink<P> wrap(Sink<P> sink)
    {
        return new RetrySink<>(sink, this.retry);
    }
}
