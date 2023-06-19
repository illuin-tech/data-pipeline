package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.execution.phase.PipelinePhaseTest.Counters;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;

import static tech.illuin.pipeline.execution.phase.PipelinePhaseTest.createPipeline;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepTest
{
    @Test
    public void testPipeline__continue()
    {
        Counters counters = new Counters();
        Pipeline<Object, ?> subPipeline = createPipeline("test-continue", (res, obj, in, ctx) -> StepStrategy.CONTINUE, counters);
        Pipeline<Object, ?> pipeline = Pipeline.ofSimple("test-enclosing-continue").registerStep(subPipeline.asStep()).build();

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);
        Assertions.assertDoesNotThrow(subPipeline::close);

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
        Pipeline<Object, ?> subPipeline = createPipeline("test-stop", (res, obj, in, ctx) -> StepStrategy.STOP, counters);
        Pipeline<Object, ?> pipeline = Pipeline.ofSimple("test-enclosing-stop").registerStep(subPipeline.asStep()).build();

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);
        Assertions.assertDoesNotThrow(subPipeline::close);

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
        Pipeline<Object, ?> subPipeline = createPipeline("test-abort", (res, obj, in, ctx) -> StepStrategy.ABORT, counters);
        Pipeline<Object, ?> pipeline = Pipeline.ofSimple("test-enclosing-abort").registerStep(subPipeline.asStep()).build();

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);
        Assertions.assertDoesNotThrow(subPipeline::close);

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
        Pipeline<Object, ?> subPipeline = createPipeline("test-exit", (res, obj, in, ctx) -> StepStrategy.EXIT, counters);
        Pipeline<Object, ?> pipeline = Pipeline.ofSimple("test-enclosing-exit").registerStep(subPipeline.asStep()).build();

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);
        Assertions.assertDoesNotThrow(subPipeline::close);

        Assertions.assertEquals(1, counters.step_evaluated.get());
        Assertions.assertEquals(0, counters.step_standard.get());
        Assertions.assertEquals(0, counters.step_pinned.get());
        Assertions.assertEquals(0, counters.sink_sync.get());
        Assertions.assertEquals(0, counters.sink_async.get());
    }
}
