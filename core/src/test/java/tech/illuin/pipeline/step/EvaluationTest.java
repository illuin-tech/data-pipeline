package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.interruption.Interruption;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;
import static tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator.*;
import static tech.illuin.pipeline.step.execution.evaluator.StepStrategy.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class EvaluationTest
{
    @Test
    public void testPipeline_shouldAbort()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(EvaluationTest::createAbortingPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "4"), getResultTypes(output, output.payload(A.class)));
        Assertions.assertIterableEquals(List.of("2", "3"), getResultTypes(output, output.payload(A.class).bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload(A.class).bs().get(1)));

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

        Pipeline<?> pipeline = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-evaluation-skip")
            .registerStep(builder -> builder
                .step(new TestStep<>("1", in -> {
                    counter.incrementAndGet();
                    return "ok";
                }))
                .withEvaluation(ALWAYS_SKIP)
            )
            .build()
        );

        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
        Assertions.assertEquals(0, output.results().stream().count());
    }

    @Test
    public void testPipeline_shouldSkipOnInterrupt()
    {
        testPipeline__onInterrupt("test-evaluation-skip-on-interrupt", SKIP_ON_INTERRUPT, false, 2, TestResult.class);
        testPipeline__onInterrupt("test-evaluation-skip-on-interrupt", SKIP_ON_INTERRUPT, true, 1, TestResult.class);
    }

    @Test
    public void testPipeline_shouldAbortOnInterrupt()
    {
        testPipeline__onInterrupt("test-evaluation-abort-on-interrupt", ABORT_ON_INTERRUPT, false, 2, TestResult.class);
        testPipeline__onInterrupt("test-evaluation-abort-on-interrupt", ABORT_ON_INTERRUPT, true, 1, Interruption.class);
    }

    @Test
    public void testPipeline_shouldStopOnInterrupt()
    {
        testPipeline__onInterrupt("test-evaluation-stop-on-interrupt", STOP_ON_INTERRUPT, false, 2, TestResult.class);
        testPipeline__onInterrupt("test-evaluation-stop-on-interrupt", STOP_ON_INTERRUPT, true, 1, Interruption.class);
    }

    @Test
    public void testPipeline_shouldDiscardOnInterrupt()
    {
        testPipeline__onInterrupt("test-evaluation-discard-on-interrupt", DISCARD_ON_INTERRUPT, false,2, TestResult.class);
        testPipeline__onInterrupt("test-evaluation-discard-on-interrupt", DISCARD_ON_INTERRUPT, true,1, Interruption.class);
    }

    private static void testPipeline__onInterrupt(String name, ResultEvaluator evaluator, boolean interrupt, int resultCount, Class<?> resultType)
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<?> pipeline = Assertions.assertDoesNotThrow(() -> Pipeline.of(name)
            .registerStep(builder -> builder
                .step((InputStep<Object>) (input, results, context) -> {
                    counter.incrementAndGet();
                    if (context.get("do_interrupt", Boolean.class).orElse(false))
                    {
                        ComponentTag tag = ((ComponentContext) context).componentTag();
                        return Interruption.of(tag, context, "stop");
                    }
                    return new TestResult("1", "ok");
                })
            )
            .registerStep(builder -> builder.step(new TestStep<>("2", "ok")))
            .setDefaultEvaluator(evaluator)
            .build()
        );

        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx -> ctx.set("do_interrupt", interrupt)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
        Assertions.assertEquals(resultCount, output.results().stream().count());
        Assertions.assertInstanceOf(resultType, output.results().stream().findFirst().orElse(null));
    }

    public static Pipeline<Void> createAbortingPipeline()
    {
        return Pipeline.of("test-abort", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializer(input, context, generator))
           .registerIndexer(SingleIndexer.auto())
           .registerIndexer((MultiIndexer<A>) A::bs)
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
