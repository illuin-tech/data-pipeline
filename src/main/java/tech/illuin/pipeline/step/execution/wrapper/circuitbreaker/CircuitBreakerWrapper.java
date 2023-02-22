package tech.illuin.pipeline.step.execution.wrapper.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CircuitBreakerWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>
{
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerWrapper(CircuitBreakerConfig config)
    {
        this.circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("circuit-breaker-" + Indexable.generateUid());
    }

    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return new CircuitBreakerStep<>(step, this.circuitBreaker);
    }
}
