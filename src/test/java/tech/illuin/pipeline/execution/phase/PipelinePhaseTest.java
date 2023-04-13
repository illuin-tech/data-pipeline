package tech.illuin.pipeline.execution.phase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelinePhaseTest
{
    @Test
    public void testPipeline__continue()
    {
        Counters counters = new Counters();
        Pipeline<Object, ?> pipeline = createPipeline("test-continue", result -> StepStrategy.CONTINUE, counters);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counters.step_evaluated.get());
        Assertions.assertEquals(1, counters.step_standard.get());
        Assertions.assertEquals(1, counters.step_pinned.get());
        Assertions.assertEquals(1, counters.sink_sync.get());
        Assertions.assertEquals(1, counters.sink_async.get());
    }

    @Test
    public void testPipeline__stop()
    {
        Counters counters = new Counters();
        Pipeline<Object, ?> pipeline = createPipeline("test-exit", result -> StepStrategy.STOP, counters);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counters.step_evaluated.get());
        Assertions.assertEquals(0, counters.step_standard.get());
        Assertions.assertEquals(1, counters.step_pinned.get());
        Assertions.assertEquals(1, counters.sink_sync.get());
        Assertions.assertEquals(1, counters.sink_async.get());
    }

    @Test
    public void testPipeline__abort()
    {
        Counters counters = new Counters();
        Pipeline<Object, ?> pipeline = createPipeline("test-abort", result -> StepStrategy.ABORT, counters);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counters.step_evaluated.get());
        Assertions.assertEquals(0, counters.step_standard.get());
        Assertions.assertEquals(0, counters.step_pinned.get());
        Assertions.assertEquals(1, counters.sink_sync.get());
        Assertions.assertEquals(1, counters.sink_async.get());
    }

    @Test
    public void testPipeline__exit()
    {
        Counters counters = new Counters();
        Pipeline<Object, ?> pipeline = createPipeline("test-exit", result -> StepStrategy.EXIT, counters);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counters.step_evaluated.get());
        Assertions.assertEquals(0, counters.step_standard.get());
        Assertions.assertEquals(0, counters.step_pinned.get());
        Assertions.assertEquals(0, counters.sink_sync.get());
        Assertions.assertEquals(0, counters.sink_async.get());
    }

    private static Pipeline<Object, VoidPayload> createPipeline(String name, ResultEvaluator evaluator, Counters counters)
    {
        return Assertions.assertDoesNotThrow(() -> Pipeline.ofSimple(name)
            .registerStep(builder -> builder
                .step((InputStep<Object>) (in, res, ctx) -> {
                    counters.step_evaluated.incrementAndGet();
                    return new TestResult("evaluated", "ok");
                })
                .withEvaluation(evaluator)
            )
            .registerStep((in, res, ctx) -> {
                counters.step_standard.incrementAndGet();
                return new TestResult("standard", "ok");
            })
            .registerStep(builder -> builder
                .step((InputStep<Object>) (in, res, ctx) -> {
                    counters.step_pinned.incrementAndGet();
                    return new TestResult("pinned", "ok");
                })
                .setPinned(true)
            )
            .registerSink((out, ctx) -> counters.sink_sync.incrementAndGet())
            .registerSink(builder -> builder
                .sink((out, ctx) -> counters.sink_async.incrementAndGet())
                .setAsync(true)
            )
            .build()
        );
    }

    public static class Counters
    {
        AtomicInteger step_evaluated = new AtomicInteger(0);
        AtomicInteger step_standard = new AtomicInteger(0);
        AtomicInteger step_pinned = new AtomicInteger(0);
        AtomicInteger sink_sync = new AtomicInteger(0);
        AtomicInteger sink_async = new AtomicInteger(0);
    }
}