package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineBuilderTest
{
    private static final AuthorResolver<Object> authorResolver = (input, context) -> "anon";
    private static final StepErrorHandler stepErrorHandler = (ex, in, p, res, ctx) -> new TestResult("stepError");
    private static final SinkErrorHandler sinkErrorHandler = (exception, output, context) -> context.get("sinkError", AtomicInteger.class).ifPresent(AtomicInteger::incrementAndGet);
    private static final ResultEvaluator resultEvaluator = (res, obj, in, ctx) -> StepStrategy.CONTINUE;
    private static final InputStep<Object> step1 = (input, results, context) -> new TestResult("1");
    private static final InputStep<Object> step2 = (input, results, context) -> new TestResult("2");
    private static final InputStep<Object> step3 = (input, results, context) -> new TestResult("3");
    private static final InputStep<Object> step4 = (input, results, context) -> new TestResult("4");
    private static final InputStep<Object> step5 = (input, results, context) -> new TestResult("5");
    private static final InputStep<Object> step6 = (input, results, context) -> new TestResult("6");
    private static final Sink sink = (output, context) -> context.get("sink", AtomicInteger.class).ifPresent(AtomicInteger::incrementAndGet);
    private static final OnCloseHandler closeHandler = () -> {};

    @Test
    public void test__simple()
    {
        var stepCounter = new StepCountObserver();
        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-simple")
            .setAuthorResolver(authorResolver)
            .setDefaultStepErrorHandler(stepErrorHandler)
            .setDefaultSinkErrorHandler(sinkErrorHandler)
            .setDefaultEvaluator(resultEvaluator)
            .registerStep(step1)
            .registerStep(b -> b.step(step2))
            .registerSteps(List.of(step3, step4))
            .registerStepAssemblers(List.of(
                b -> b.step(step5),
                b -> b.step(step6)
            ))
            .registerSink(sink)
            .registerSinks(List.of(sink, sink))
            .registerObserver(stepCounter)
            .registerOnCloseHandler(closeHandler)
        );

        Context ctx = new SimpleContext().set("sink", new AtomicInteger(0));
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx));

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals("anon", output.tag().author());
        Assertions.assertEquals(6, output.results().stream().count());
        Assertions.assertEquals("1", output.results().stream().findFirst().map(Result::name).orElse(null));
        Assertions.assertEquals("6", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
        Assertions.assertEquals(3, ctx.get("sink", AtomicInteger.class).map(AtomicInteger::get).orElse(0));
        Assertions.assertEquals(6, stepCounter.count());
    }

    @Test
    public void test__payload()
    {
        var stepCounter = new StepCountObserver();
        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of(
                "test-payload",
                (input, context, generator) -> new TestIndexable(generator.generate())
            )
            .setAuthorResolver(authorResolver)
            .setDefaultStepErrorHandler(stepErrorHandler)
            .setDefaultSinkErrorHandler(sinkErrorHandler)
            .setDefaultEvaluator(resultEvaluator)
            .registerStep(step1)
            .registerSteps(List.of(
                (payload, results, context) -> new TestResult("2"),
                (PayloadStep) (payload, results, context) -> new TestResult("3")
            ))
            .registerSink(sink)
            .registerSinks(List.of(sink, sink))
            .registerObserver(stepCounter)
            .registerOnCloseHandler(closeHandler)
        );

        Context ctx = new SimpleContext().set("sink", new AtomicInteger(0));
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals("anon", output.tag().author());
        Assertions.assertEquals(3, output.results().stream().count());
        Assertions.assertEquals("1", output.results().stream().findFirst().map(Result::name).orElse(null));
        Assertions.assertEquals("3", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
        Assertions.assertEquals(3, ctx.get("sink", AtomicInteger.class).map(AtomicInteger::get).orElse(0));
        Assertions.assertEquals(3, stepCounter.count());
    }

    @Test
    public void test__payload__withAssemblers()
    {
        InitializerAssembler<Object> initializer = b -> b
            .withId("init-builder")
            .initializer((input, context, generator) -> new TestIndexable(generator.generate()))
        ;

        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-payload", initializer)
            .setAuthorResolver(authorResolver)
            .setDefaultStepErrorHandler(stepErrorHandler)
            .setDefaultSinkErrorHandler(sinkErrorHandler)
            .setDefaultEvaluator(resultEvaluator)
            .registerStep(b -> b.withId("step-builder").step(step1))
            .registerStepAssemblers(List.of(
                b -> b.step(step2),
                b -> b.step(step3)
            ))
            .registerSink(b -> b
                .withId("sink-builder")
                .sink(sink)
                .setAsync(true)
            )
            .registerSinkAssemblers(List.of(
                b -> b.sink(sink),
                b -> b.sink(sink).setAsync(true)
            ))
            .registerOnCloseHandler(closeHandler)
        );

        Context ctx = new SimpleContext().set("sink", new AtomicInteger(0));
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals("anon", output.tag().author());
        Assertions.assertEquals(3, output.results().stream().count());
        Assertions.assertEquals("1", output.results().stream().findFirst().map(Result::name).orElse(null));
        Assertions.assertEquals("3", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
        Assertions.assertEquals(3, ctx.get("sink", AtomicInteger.class).map(AtomicInteger::get).orElse(0));
    }

    @Test
    public void test__withDefaultEvaluator()
    {
        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-with-default-evaluator")
            .registerStep(step1)
            .registerStep(step2)
            .setDefaultEvaluator((res, obj, in, ctx) -> StepStrategy.EXIT)
        );

        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals(1, output.results().stream().count());
        Assertions.assertEquals("1", output.results().stream().findFirst().map(Result::name).orElse(null));
        Assertions.assertEquals("1", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
    }

    @Test
    public void test__withDefaultStepErrorHandler()
    {
        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-with-default-step-error-handler")
            .registerStep((input, results, context) -> { throw new Exception("interrupted"); })
            .registerStep(step2)
            .setDefaultStepErrorHandler(stepErrorHandler)
        );

        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals(2, output.results().stream().count());
        Assertions.assertEquals("stepError", output.results().stream().findFirst().map(Result::name).orElse(null));
        Assertions.assertEquals("2", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
    }

    @Test
    public void test__withDefaultSinkErrorHandler()
    {
        var builder = Assertions.assertDoesNotThrow(() -> Pipeline.of("test-with-default-sink-error-handler")
            .registerStep(step1)
            .registerStep(step2)
            .registerSink((output, context) -> { throw new Exception("interrupted"); })
            .setDefaultSinkErrorHandler(sinkErrorHandler)
        );

        Context ctx = new SimpleContext().set("sinkError", new AtomicInteger(0));
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(builder::build);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx));

        Assertions.assertEquals(builder.id(), output.tag().pipeline());
        Assertions.assertEquals(2, output.results().stream().count());
        Assertions.assertEquals("2", output.results().latest(TestResult.class).map(TestResult::name).orElse(null));
        Assertions.assertEquals(1, ctx.get("sinkError", AtomicInteger.class).map(AtomicInteger::get).orElse(0));
    }

    public record TestIndexable(
        String uid
    ) implements Indexable {}

    public record TestResult(
        String name
    ) implements Result {}

    private static class StepCountObserver implements Observer
    {
        private final AtomicInteger counter = new AtomicInteger(0);

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
            this.counter.addAndGet(steps.size());
        }

        public int count()
        {
            return this.counter.get();
        }
    }
}
