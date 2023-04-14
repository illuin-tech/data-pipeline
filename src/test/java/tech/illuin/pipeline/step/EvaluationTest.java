package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.output.Output;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;
import static tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator.ALWAYS_SKIP;
import static tech.illuin.pipeline.step.execution.evaluator.StepStrategy.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class EvaluationTest
{
    @Test
    public void testPipeline_shouldAbort()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(EvaluationTest::createAbortingPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "4"), getResultTypes(output, output.payload()));
        Assertions.assertIterableEquals(List.of("2", "3"), getResultTypes(output, output.payload().bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload().bs().get(1)));

        Assertions.assertEquals(3, output.results().size());
        Assertions.assertEquals(5, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
        Assertions.assertTrue(output.results().current("4").isPresent());
        Assertions.assertTrue(output.results().current("5").isEmpty());
    }

    @Test
    public void testPipeline_shouldSkip()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> Pipeline.ofSimple("test-evaluation-skip")
            .registerStep(builder -> builder
                .step(new TestStep<>("1", in -> {
                    counter.incrementAndGet();
                    return "ok";
                }))
                .withEvaluation(ALWAYS_SKIP)
            )
            .build()
        );

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
        Assertions.assertEquals(0, output.results().stream().count());
    }

    public static Pipeline<Void, A> createAbortingPipeline()
    {
        return Pipeline.ofPayload("test-abort", TestFactory::initializer)
           .registerIndexer(SingleIndexer.auto())
           .registerIndexer(A::bs)
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<B>("2", b -> b.name().equals("b1") ? "ok" : "ko"))
               .withEvaluation((res, obj, in, ctx) -> ((TestResult) res).status().equals("ok") ? CONTINUE : DISCARD_AND_CONTINUE)
               .withCondition(B.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("3", "ok"))
               .withCondition(B.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("4", "ko"))
               .withEvaluation((res, obj, in, ctx) -> ABORT)
               .withCondition(A.class)
           )
           .registerStep(new TestStep<>("5", "ok"))
           .build()
        ;
    }
}
