package tech.illuin.pipeline.observer;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.execution.phase.PipelinePhaseTest.Counters;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.observer.descriptor.DescriptionNotAvailableException;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static tech.illuin.pipeline.execution.phase.PipelinePhaseTest.createPipeline;
import static tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator.ALWAYS_CONTINUE;

public class PipelineTest
{
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
    public void test__shouldDescribeCompositePipeline()
    {
        var pipeline = createPipeline("composite-describe", ALWAYS_CONTINUE, new Counters());

        PipelineDescription description = Assertions.assertDoesNotThrow(pipeline::describe);

        Assertions.assertEquals("composite-describe", description.id());

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
}
