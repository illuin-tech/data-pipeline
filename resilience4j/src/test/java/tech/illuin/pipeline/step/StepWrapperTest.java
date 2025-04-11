package tech.illuin.pipeline.step;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.PayloadPipelineBuilder;
import tech.illuin.pipeline.generic.wrapper.config.TestTimeLimiterStepHandler;
import tech.illuin.pipeline.generic.wrapper.config.TestRetryStepHandler;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.resilience4j.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry.RetryWrapperConfig;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter.TimeLimiterWrapperConfig;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;
import static tech.illuin.pipeline.generic.Tests.sleep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWrapperTest
{
    @Test
    public void testPipeline_shouldCombineWrappers()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(StepWrapperTest::createPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "2", "3"), getResultTypes(output, output.payload(A.class)));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    @Test
    public void testPipeline_shouldUseWrapperConfig()
    {
        TestTimeLimiterStepHandler timeLimiterHandler = new TestTimeLimiterStepHandler("time-limiter");
        TestRetryStepHandler retryHandler = new TestRetryStepHandler("retry");

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithWrapperConfig(
            TimeLimiterWrapperConfig.Builder.builder().setStepHandler(timeLimiterHandler).build(),
            RetryWrapperConfig.Builder.builder().setStepHandler(retryHandler).build()
        ));
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "2", "3"), getResultTypes(output, output.payload(A.class)));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());

        Assertions.assertEquals(1, timeLimiterHandler.onSuccessCounter().get());
        Assertions.assertEquals(2, timeLimiterHandler.onErrorCounter().get());
        Assertions.assertEquals(1, retryHandler.onSuccessCounter().get());
        Assertions.assertEquals(3, retryHandler.onRetryCounter().get());
        Assertions.assertEquals(0, retryHandler.onErrorCounter().get());
    }

    private static Pipeline<Void> createPipeline()
    {
        StepWrapper<Indexable, Void> timeWrapper = new TimeLimiterWrapper<>(
            TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build()
        );
        StepWrapper<Indexable, Void> retryWrapper = new RetryWrapper<>(
            RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(25)).build()
        );

        return createPipelineWithWrappers(timeWrapper, retryWrapper, null);
    }

    private static Pipeline<Void> createPipelineWithWrapperConfig(TimeLimiterWrapperConfig firstWrapperConfig, RetryWrapperConfig secondWrapperConfig)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        StepWrapper<Indexable, Void> timeWrapper = new TimeLimiterWrapper<>(
            TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build(),
            executorService,
            firstWrapperConfig
        );
        StepWrapper<Indexable, Void> retryWrapper = new RetryWrapper<>(
            RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(25)).build(),
            secondWrapperConfig
        );

        return createPipelineWithWrappers(timeWrapper, retryWrapper, executorService);
    }

    private static Pipeline<Void> createPipelineWithWrappers(StepWrapper<Indexable, Void> firstWrapper, StepWrapper<Indexable, Void> secondWrapper, ExecutorService executorService)
    {
        AtomicInteger counter = new AtomicInteger(0);
        PayloadPipelineBuilder<Void> builder =  Pipeline.of("test-wrapper-combined", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializerOfEmpty(input, context, generator))
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestStep<>("1", "ok"))
            .registerStep(sb -> sb
                .withId("retried-test-step")
                .step(new TestStep<>("2", b -> {
                    if (counter.getAndIncrement() < 2)
                        sleep(Duration.ofMillis(500));
                    return "ok";
                }))
                .withWrapper(firstWrapper.andThen(secondWrapper))
            )
            .registerStep(new TestStep<>("3", "ok"));

        if (executorService != null)
            builder.registerOnCloseHandler(executorService::shutdown);

        return builder.build();
    }
}
