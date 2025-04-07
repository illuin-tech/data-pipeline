package tech.illuin.pipeline.sink;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.PayloadPipelineBuilder;
import tech.illuin.pipeline.generic.wrapper.config.TestTimeLimiterSinkHandler;
import tech.illuin.pipeline.generic.wrapper.config.TestRetrySinkHandler;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetryWrapperConfig;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterWrapperConfig;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> SinkWrapperTest.createPipeline(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(3, counter.get());
    }

    @Test
    public void testPipeline_shouldUseWrapperConfig() {
        AtomicInteger counter = new AtomicInteger(0);

        TestTimeLimiterSinkHandler timeLimiterHandler = new TestTimeLimiterSinkHandler("time-limiter");
        TestRetrySinkHandler retryHandler = new TestRetrySinkHandler("retry");

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> SinkWrapperTest.createPipelineWithWrapperConfig(
            TimeLimiterWrapperConfig.Builder.builder().setSinkHandler(timeLimiterHandler).build(),
            RetryWrapperConfig.Builder.builder().setSinkHandler(retryHandler).build(),
            counter
        ));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(3, counter.get());

        Assertions.assertEquals(1, timeLimiterHandler.onSuccessCounter().get());
        Assertions.assertEquals(2, timeLimiterHandler.onErrorCounter().get());
        Assertions.assertEquals(1, retryHandler.onSuccessCounter().get());
        Assertions.assertEquals(3, retryHandler.onRetryCounter().get());
        Assertions.assertEquals(0, retryHandler.onErrorCounter().get());
    }

    public static Pipeline<Void> createPipeline(AtomicInteger counter)
    {
        SinkWrapper timeWrapper = new TimeLimiterWrapper<>(
            TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build()
        );
        SinkWrapper retryWrapper = new RetryWrapper<>(
            RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(25)).build()
        );

        return createPipelineWithWrappers(timeWrapper, retryWrapper, counter, null);
    }

    private static Pipeline<Void> createPipelineWithWrapperConfig(TimeLimiterWrapperConfig firstWrapperConfig, RetryWrapperConfig secondWrapperConfig, AtomicInteger counter)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        SinkWrapper timeWrapper = new TimeLimiterWrapper<>(
            TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build(),
            executorService,
            firstWrapperConfig
        );
        SinkWrapper retryWrapper = new RetryWrapper<>(
            RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(25)).build(),
            secondWrapperConfig
        );

        return createPipelineWithWrappers(timeWrapper, retryWrapper, counter, executorService);
    }

    private static Pipeline<Void> createPipelineWithWrappers(SinkWrapper firstWrapper, SinkWrapper secondWrapper, AtomicInteger counter, ExecutorService executorService)
    {
        PayloadPipelineBuilder<Void> builder = Pipeline.of("test-wrapper-combined", (Void input, LocalContext context1, UIDGenerator generator) -> TestFactory.initializerOfEmpty(input, context1, generator))
            .registerIndexer(SingleIndexer.auto())
            .registerSink(b -> b
                .sink((output, context) -> {
                    if (counter.getAndIncrement() < 2)
                        sleep(Duration.ofMillis(500));
                })
                .withWrapper(firstWrapper.andThen(secondWrapper))
            );

        if (executorService != null)
            builder.registerOnCloseHandler(executorService::shutdown);

        return builder.build();
    }
}
