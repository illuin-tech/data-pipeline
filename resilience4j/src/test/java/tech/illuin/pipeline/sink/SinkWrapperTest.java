package tech.illuin.pipeline.sink;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.TestFactory;
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

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> SinkWrapperTest.createPipeline(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(3, counter.get());
    }

    public static Pipeline<Void> createPipeline(AtomicInteger counter)
    {
        SinkWrapper timeWrapper = new TimeLimiterWrapper<>(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(200))
            .build()
        );
        SinkWrapper retryWrapper = new RetryWrapper<>(RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(25))
            .build()
        );

        return Pipeline.of("test-wrapper-combined", (Void input, LocalContext context1, UIDGenerator generator) -> TestFactory.initializerOfEmpty(input, context1, generator))
            .registerIndexer(SingleIndexer.auto())
            .registerSink(builder -> builder
                .sink((output, context) -> {
                    if (counter.getAndIncrement() < 2)
                        sleep(Duration.ofMillis(500));
                })
                .withWrapper(timeWrapper.andThen(retryWrapper))
            )
            .build()
        ;
    }
}
