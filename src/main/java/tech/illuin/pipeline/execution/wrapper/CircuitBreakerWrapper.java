package tech.illuin.pipeline.execution.wrapper;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.sink.execution.wrapper.circuitbreaker.CircuitBreakerSink;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.execution.wrapper.circuitbreaker.CircuitBreakerStep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>, SinkWrapper<P>
{
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerWrapper(CircuitBreakerConfig config)
    {
        this.circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("circuit-breaker-" + KSUIDGenerator.INSTANCE.generate());
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new CircuitBreakerStep<>(step, this.circuitBreaker);
    }

    @Override
    public Sink<P> wrap(Sink<P> sink)
    {
        return new CircuitBreakerSink<>(sink, this.circuitBreaker);
    }
}
