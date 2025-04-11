package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepErrorHandlerTest
{
    @Test
    public void testPipeline_shouldThrowException()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(StepErrorHandlerTest::createErrorThrownPipeline);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, pipeline::run);
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("Some error", exception.getMessage());
    }

    @Test
    public void testPipeline_shouldHandleError()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(StepErrorHandlerTest::createErrorHandledPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "error"), getResultTypes(output, output.payload(A.class)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload(A.class).bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload(A.class).bs().get(1)));

        Assertions.assertEquals(3, output.results().size());
        Assertions.assertEquals(4, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isEmpty());
        Assertions.assertTrue(output.results().current("4").isEmpty());
        Assertions.assertTrue(output.results().current("error").isPresent());
    }

    @Test
    public void testPipeline_firstErrorHandlerFails_shouldHandleError()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> createErrorHandledPipelineWithTwoErrorHandlers(counter));
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "error"), getResultTypes(output, output.payload(A.class)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload(A.class).bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload(A.class).bs().get(1)));

        Assertions.assertEquals(3, output.results().size());
        Assertions.assertEquals(4, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isEmpty());
        Assertions.assertTrue(output.results().current("4").isEmpty());
        Assertions.assertTrue(output.results().current("error").isPresent());
        Assertions.assertEquals(1, counter.get());
    }

    public static Pipeline<Void> createErrorHandledPipeline()
    {
        return Pipeline.of("test-error-handled", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializer(input, context, generator))
           .registerIndexer(SingleIndexer.auto())
           .registerIndexer((MultiIndexer<A>) A::bs)
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("2", "ok"))
               .withCondition(B.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("3", b -> { throw new RuntimeException("Some error"); }))
               .withErrorHandler((ex, in, payload, res, ctx) -> new TestResult("error", "ko"))
               .withEvaluation(ResultEvaluator.ALWAYS_ABORT)
               .withCondition(A.class)
           )
           .registerStep(new TestStep<>("4", "ok"))
           .build()
        ;
    }

    public static Pipeline<Void> createErrorHandledPipelineWithTwoErrorHandlers(AtomicInteger counter)
    {
        StepErrorHandler firstErrorHandler = (ex, in, payload, res, ctx) -> {
            counter.incrementAndGet();
            throw new Exception("Some error again");
        };
        StepErrorHandler secondErrorhandler = (ex, in, payload, res, ctx) -> new TestResult("error", "ko");

        return Pipeline.of("test-composite-error-handled", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializer(input, context, generator))
            .registerIndexer(SingleIndexer.auto())
            .registerIndexer((MultiIndexer<A>) A::bs)
            .registerStep(builder -> builder
                .step(new TestStep<>("1", "ok"))
                .withCondition(A.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("2", "ok"))
                .withCondition(B.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("3", b -> { throw new RuntimeException("Some error"); }))
                .withErrorHandler(firstErrorHandler.andThen(secondErrorhandler))
                .withEvaluation(ResultEvaluator.ALWAYS_ABORT)
                .withCondition(A.class)
            )
            .registerStep(new TestStep<>("4", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void> createErrorThrownPipeline()
    {
        return Pipeline.of("test-error-thrown", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializerOfEmpty(input, context, generator))
           .registerIndexer(SingleIndexer.auto())
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("2", b -> { throw new RuntimeException("Some error"); }))
               .withCondition(A.class)
           )
           .registerStep(new TestStep<>("3", "ok"))
           .build()
        ;
    }
}
