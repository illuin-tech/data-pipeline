package tech.illuin.pipeline.sink;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.sleep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkWrapperTest
{
    @Test
    public void testPipeline_shouldCombineWrappers()
    {
        var counter = new AtomicInteger(0);

        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(() -> SinkWrapperTest.createPipeline(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(5, counter.get());
    }

    public static Pipeline<Void, A> createPipeline(AtomicInteger counter)
    {
        SinkWrapper<A> timeWrapper = new TimeLimiterWrapper<>(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(2))
            .build()
        );
        SinkWrapper<A> retryWrapper = new RetryWrapper<>(RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofMillis(500))
            .build()
        );

        return Pipeline.ofPayload("test-wrapper-combined", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerSink(builder -> builder
                .sink((output, context) -> {
                    if (counter.getAndIncrement() < 4)
                        sleep(Duration.ofSeconds(5));
                })
                .withWrapper(timeWrapper.andThen(retryWrapper))
            )
            .build()
        ;
    }
}
