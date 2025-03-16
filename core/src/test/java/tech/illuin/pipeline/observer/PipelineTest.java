package tech.illuin.pipeline.observer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.execution.phase.PipelinePhaseTest.Counters;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.observer.PipelineTest.StepWithDescription.MyStepDescription;
import tech.illuin.pipeline.observer.descriptor.DescriptionNotAvailableException;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.observer.descriptor.describable.Description;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static tech.illuin.pipeline.execution.phase.PipelinePhaseTest.createPipeline;
import static tech.illuin.pipeline.metering.MeterRegistryKey.PIPELINE_RUN_SUCCESS_KEY;
import static tech.illuin.pipeline.step.annotation.PipelineStepAnnotationTest.createPipeline_latest;
import static tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator.ALWAYS_CONTINUE;

public class PipelineTest
{
    @Test
    public void test__shouldDescribeCompositePipeline()
    {
        var meterRegistry = new SimpleMeterRegistry();
        var pipeline = createPipeline(
            "composite-describe",
            ALWAYS_CONTINUE,
            new Counters(),
            builder -> builder.addObservabilityComponent(meterRegistry)
        );

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        PipelineDescription description = Assertions.assertDoesNotThrow(pipeline::describe);

        Assertions.assertEquals("composite-describe", description.id());
        Counter counter = Assertions.assertInstanceOf(Counter.class, meterRegistry.getMeters().stream()
            .filter(m -> m.getId().getName().equals(PIPELINE_RUN_SUCCESS_KEY.id()))
            .findFirst().orElse(null));
        Assertions.assertEquals(
            counter.count(),
            description.metrics().get(PIPELINE_RUN_SUCCESS_KEY.id()).values().get(counter.getId())
        );

        Assertions.assertEquals("anonymous-init", description.init().id());
        String initializerDesc = Assertions.assertInstanceOf(String.class, description.init().initializer());
        Assertions.assertTrue(initializerDesc.startsWith("tech.illuin.pipeline.builder.SimplePipelineBuilder$$Lambda"));
        String initializerErrorHandlerDesc = Assertions.assertInstanceOf(String.class, description.init().errorHandler());
        Assertions.assertTrue(initializerErrorHandlerDesc.startsWith("tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler$$Lambda"));

        Assertions.assertEquals(3, description.steps().size());
        String stepDesc = Assertions.assertInstanceOf(String.class, description.steps().get(0).step());
        Assertions.assertTrue(stepDesc.startsWith("tech.illuin.pipeline.execution.phase.PipelinePhaseTest$$Lambda"));
        String stepConditionDesc = Assertions.assertInstanceOf(String.class, description.steps().get(0).condition());
        Assertions.assertTrue(stepConditionDesc.startsWith("tech.illuin.pipeline.step.execution.condition.StepCondition$$Lambda"));
        String stepEvaluatorDesc = Assertions.assertInstanceOf(String.class, description.steps().get(0).resultEvaluator());
        Assertions.assertTrue(stepEvaluatorDesc.startsWith("tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator$$Lambda"));
        String stepWrapperDesc = Assertions.assertInstanceOf(String.class, description.steps().get(0).executionWrapper());
        Assertions.assertTrue(stepWrapperDesc.startsWith("tech.illuin.pipeline.step.builder.StepBuilder$$Lambda"));
        String stepErrorHandlerDesc = Assertions.assertInstanceOf(String.class, description.steps().get(0).errorHandler());
        Assertions.assertTrue(stepErrorHandlerDesc.startsWith("tech.illuin.pipeline.step.execution.error.StepErrorHandler$$Lambda"));
        Assertions.assertFalse(description.steps().get(0).pinned());

        Assertions.assertEquals(2, description.sinks().size());
        String sinkDesc = Assertions.assertInstanceOf(String.class, description.sinks().get(0).sink());
        Assertions.assertTrue(sinkDesc.startsWith("tech.illuin.pipeline.execution.phase.PipelinePhaseTest$$Lambda"));
        String sinkWrapperDesc = Assertions.assertInstanceOf(String.class, description.sinks().get(0).executionWrapper());
        Assertions.assertTrue(sinkWrapperDesc.startsWith("tech.illuin.pipeline.sink.builder.SinkBuilder$$Lambda"));
        String sinkErrorHandlerDesc = Assertions.assertInstanceOf(String.class, description.sinks().get(0).errorHandler());
        Assertions.assertTrue(sinkErrorHandlerDesc.startsWith("tech.illuin.pipeline.sink.execution.error.SinkErrorHandler$$Lambda"));
        Assertions.assertFalse(description.sinks().get(0).isAsync());

        Assertions.assertDoesNotThrow(pipeline::close);
    }

    @Test
    public void test__shouldFailOnLambdaPipeline()
    {
        Pipeline<String> pipeline = (input, ctx) -> new Output(
            new PipelineTag("abc", "def", "ghi"),
            null,
            ctx
        );

        Assertions.assertThrows(DescriptionNotAvailableException.class, pipeline::describe);
    }

    @Test
    public void test__shouldCloseObservers()
    {
        var observer = new OpenClosedObserver();
        Assertions.assertFalse(observer.isOpen());

        var pipeline = createPipeline(
            "composite-close",
            ALWAYS_CONTINUE,
            new Counters(),
            builder -> builder.registerObserver(observer)
        );
        Assertions.assertTrue(observer.isOpen());

        Assertions.assertDoesNotThrow(pipeline::close);
        Assertions.assertFalse(observer.isOpen());
    }

    @Test
    public void test__shouldHandleRecursiveDescription()
    {
        var pipeline = Assertions.assertDoesNotThrow(
            () -> createPipeline_latest(
                "recursive-describe",
                builder -> builder.registerStep(new StepWithDescription())
            )
        );

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        PipelineDescription description = Assertions.assertDoesNotThrow(pipeline::describe);

        Assertions.assertEquals(5, description.steps().size());
        Assertions.assertEquals("tech.illuin.pipeline.step.annotation.step.StepWithInput", description.steps().get(0).step());
        Assertions.assertEquals("tech.illuin.pipeline.step.annotation.step.StepWithInputAndLatest", description.steps().get(1).step());
        Assertions.assertEquals("tech.illuin.pipeline.step.annotation.step.StepWithInputAndLatestOptional", description.steps().get(2).step());
        Assertions.assertEquals("tech.illuin.pipeline.step.annotation.step.StepWithInputAndLatestStream", description.steps().get(3).step());
        Assertions.assertEquals(new MyStepDescription("StepWithDescription", 12), description.steps().get(4).step());
    }

    private static class OpenClosedObserver implements Observer
    {
        private final AtomicBoolean state = new AtomicBoolean(false);

        @Override
        public <I> void init(
            String id,
            InitializerDescriptor<I> initializer,
            List<StepDescriptor<Indexable, I>> steps,
            List<SinkDescriptor> sinks,
            PipelineErrorHandler errorHandler,
            List<OnCloseHandler> onCloseHandlers,
            MeterRegistry meterRegistry
        ) {
            this.state.set(true);
        }

        @Override
        public void close()
        {
            this.state.set(false);
        }

        public boolean isOpen()
        {
            return this.state.get();
        }
    }

    public static class StepWithDescription implements Describable
    {
        @StepConfig(id = "step-with_description")
        public Result execute()
        {
            return new TestResult("annotation-test", "whatever");
        }

        @Override
        public Description describe()
        {
            return new MyStepDescription(this.getClass().getSimpleName(), 12);
        }

        public record MyStepDescription(
            String name,
            int value
        ) implements Description {}
    }
}
