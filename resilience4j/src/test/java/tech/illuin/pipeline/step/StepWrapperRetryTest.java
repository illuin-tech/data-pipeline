package tech.illuin.pipeline.step;

import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.resilience4j.execution.wrapper.RetryWrapper;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.output.Output;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWrapperRetryTest
{
    @Test
    public void testPipeline_shouldRetryException()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(StepWrapperRetryTest::createErrorRetryPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "2", "3"), getResultTypes(output, output.payload(A.class)));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    public static Pipeline<Void> createErrorRetryPipeline()
    {
        var counter = new AtomicInteger(0);
        return Pipeline.of("test-error-retry", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestStep<>("1", "ok"))
            .registerStep(builder -> builder
                .step(new TestStep<>("2", b -> {
                   if (counter.getAndIncrement() < 4)
                       throw new RuntimeException("Some error");
                   return "ok";
                }))
                .withWrapper(new RetryWrapper<>(RetryConfig.custom()
                    .maxAttempts(5)
                    .waitDuration(Duration.ofMillis(500))
                    .build()
                ))
            )
            .registerStep(new TestStep<>("3", "ok"))
            .build()
        ;
    }
}
