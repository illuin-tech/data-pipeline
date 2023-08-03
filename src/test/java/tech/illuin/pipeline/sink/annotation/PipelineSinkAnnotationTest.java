package tech.illuin.pipeline.sink.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.annotation.sink.*;
import tech.illuin.pipeline.sink.annotation.sink.SinkWithException.SinkWithExceptionException;
import tech.illuin.pipeline.step.annotation.step.StepWithInput;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkAnnotationTest
{
    @Test
    public void testPipeline__shouldCompile_input()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_assembler()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_assembler("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_of()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_of("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_exception()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_exception("test-input"));

        PipelineException exception = Assertions.assertThrows(PipelineException.class, () -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertTrue(exception.getCause() instanceof SinkWithExceptionException);
    }

    @Test
    public void testPipeline__shouldCompile_inputAndReturnValue()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_returnValue("test-input-return-value", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_payload()
    {
        StringCollector collector = new StringCollector();
        Pipeline<String, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_payload("test-payload", collector));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(output.payload().object().uid(), collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_output()
    {
        StringCollector collector = new StringCollector();
        Pipeline<String, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_output("test-output", collector));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(output.payload().object().uid(), collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_current()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_current("test-current", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_currentOptional()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentOptional("test-current-optional", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_currentStream()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentStream("test-current-stream", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_latest()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latest("test-latest", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_latestOptional()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestOptional("test-latest-optional", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_latestStream()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestStream("test-latest-stream", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input->input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_tags()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_tags("test-tags", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "test-tags#sink-with_tags",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_results()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_results("test-results", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input",
            collector.get()
        );
    }

    @Test
    public void testPipeline__shouldCompile_context()
    {
        String key = "test_key";
        String value = "my_value";

        StringCollector collector = new StringCollector();
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_context("test-context", key, collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx.set(key, value)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(value, collector.get());
    }

    public static Pipeline<Object, ?> createPipeline_input(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(new SinkWithInput<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_assembler(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(builder -> builder.sink(new SinkWithInput<>(collector)))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_of(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(Sink.of(new SinkWithInput<>(collector)))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_exception(String name)
    {
        return Pipeline.ofSimple(name)
            .registerSink(Sink.of(new SinkWithException<>()))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_returnValue(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(new SinkWithInputAndReturnValue<>(collector))
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_payload(String name, StringCollector collector)
    {
        return Pipeline.ofPayload(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerSink(new SinkWithPayload(collector))
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_output(String name, StringCollector collector)
    {
        return Pipeline.ofPayload(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerSink(new SinkWithOutput(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_current(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrent<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_currentOptional(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrentOptional<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_currentStream(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrentStream<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latest(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatest<>(collector))
            .registerSink(new SinkWithInputAndLatestOptional<>(collector))
            .registerSink(new SinkWithInputAndLatestStream<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latestOptional(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatestOptional<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latestStream(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatestStream<>(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_tags(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(new SinkWithTags(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_results(String name, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithResults(collector))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_context(String name, String metadataKey, StringCollector collector)
    {
        return Pipeline.ofSimple(name)
            .registerSink(new SinkWithContext(metadataKey, collector))
            .build()
        ;
    }

    public static class StringCollector
    {
        private String value;

        public String update(String value)
        {
            this.value = value;
            return this.value;
        }

        public String get()
        {
            return this.value;
        }
    }

    public record TestPayload(
        TestObject object
    ) {}

    public record TestObject(
        String uid
    ) implements Indexable {}
}
