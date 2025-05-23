package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PinTest
{
    @Test
    public void testPipeline_shouldHandleErrorWithPinned()
    {
        this.testPipeline_shouldHandleErrorWithPinned__runTest(ResultEvaluator.ALWAYS_CONTINUE, List.of("1", "3", "error"), 3, Optional::isPresent);
        this.testPipeline_shouldHandleErrorWithPinned__runTest(ResultEvaluator.ALWAYS_DISCARD, List.of("1", "3", "error"), 3, Optional::isPresent);
        this.testPipeline_shouldHandleErrorWithPinned__runTest(ResultEvaluator.ALWAYS_STOP, List.of("1", "3", "error"), 3, Optional::isPresent);
        this.testPipeline_shouldHandleErrorWithPinned__runTest(ResultEvaluator.ALWAYS_ABORT, List.of("1", "error"), 2, Optional::isEmpty);
    }

    private void testPipeline_shouldHandleErrorWithPinned__runTest(ResultEvaluator evaluator, List<String> tags, int resultCount, Predicate<Optional<Result>> predicate4)
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> PinTest.createErrorHandledWithPinnedPipeline(evaluator));
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(tags, getResultTypes(output, output.payload(A.class)));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(resultCount, output.results().current().count());

        Assertions.assertTrue(predicate4.test(output.results().current("3")));
        Assertions.assertTrue(output.results().current("error").isPresent());
    }

    public static Pipeline<Void> createErrorHandledWithPinnedPipeline(ResultEvaluator evaluator)
    {
        return Pipeline.of("test-error-handled-with-pinned", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializer(input, context, generator))
           .registerIndexer(SingleIndexer.auto())
           .registerIndexer((MultiIndexer<A>) A::bs)
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withId("step-1")
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("2", b -> { throw new RuntimeException("Some error"); }))
               .withId("step-2")
               .withErrorHandler((ex, in, payload, res, ctx) -> new TestResult("error", "ko"))
               .withEvaluation(evaluator)
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("3", "ok"))
               .withId("step-3")
               .withCondition(A.class)
               .setPinned(true)
           )
           .build()
        ;
    }
}
