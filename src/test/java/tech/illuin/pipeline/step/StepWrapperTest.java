package tech.illuin.pipeline.step;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;

import java.time.Duration;
import java.util.List;
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
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(StepWrapperTest::createPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "2", "3"), getResultTypes(output, output.payload()));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    public static Pipeline<Void, A> createPipeline()
    {
        StepWrapper<Indexable, Void, A> timeWrapper = new TimeLimiterWrapper<>(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(2))
            .build()
        );
        StepWrapper<Indexable, Void, A> retryWrapper = new RetryWrapper<>(RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofMillis(500))
            .build()
        );

        var counter = new AtomicInteger(0);
        return Pipeline.ofPayload("test-wrapper-combined", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestStep<>("1", "ok"))
            .registerStep(builder -> builder
                .step(new TestStep<>("2", b -> {
                    if (counter.getAndIncrement() < 4)
                        sleep(Duration.ofSeconds(5));
                    return "ok";
                }))
                .withWrapper(timeWrapper.andThen(retryWrapper))
            )
            .registerStep(new TestStep<>("3", "ok"))
            .build()
        ;
    }
}
