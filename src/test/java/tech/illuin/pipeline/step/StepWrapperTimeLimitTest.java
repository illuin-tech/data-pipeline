package tech.illuin.pipeline.step;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.execution.wrapper.TimeLimiterWrapper;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepBuilder;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;
import static tech.illuin.pipeline.generic.Tests.sleep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepWrapperTimeLimitTest
{
    @Test
    public void testPipeline_shouldTimeout()
    {
        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createTimeoutPipeline(StepWrapperTimeLimitTest::addTimeLimitedStep));

        var ex = Assertions.assertThrows(RuntimeException.class, pipeline::run);
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertTrue(ex.getCause() instanceof TimeoutException);
    }

    @Test
    public void testPipeline_shouldTimeoutThenBeHandled()
    {
        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createTimeoutPipeline(StepWrapperTimeLimitTest::addTimeLimitedStepWithErrorHandler));
        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "3", "timed-out"), getResultTypes(output));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("timed-out").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    public static Pipeline<?, VoidPayload> createTimeoutPipeline(StepAssembler<?, Object, VoidPayload> timeLimitedAssembler)
    {
        return Pipeline.ofSimple("test-error-timelimit")
            .registerStep(new TestStep<>("1", "ok"))
            .registerStep(timeLimitedAssembler)
            .registerStep(new TestStep<>("3", "ok"))
            .build()
        ;
    }

    public static void addTimeLimitedStep(StepBuilder<?, ?, ?> builder)
    {
        builder
            .step(new TestStep<>("2", b -> {
                sleep(Duration.ofSeconds(5));
                return "ok";
            }))
            .withWrapper(new TimeLimiterWrapper<>(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))
                .build()
            ))
        ;
    }

    public static void addTimeLimitedStepWithErrorHandler(StepBuilder<?, ?, ?> builder)
    {
        addTimeLimitedStep(builder);
        builder.withErrorHandler((exception, input, payload, results, context) -> new TestResult("timed-out", "ko"));
    }
}
