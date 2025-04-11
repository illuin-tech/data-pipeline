package tech.illuin.pipeline.sink.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.annotation.sink.*;
import tech.illuin.pipeline.sink.annotation.sink.SinkWithException.SinkWithExceptionException;
import tech.illuin.pipeline.step.annotation.step.StepWithInput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkAnnotationTest
{
    @Test
    public void testPipeline__shouldCompile_input()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_assembler()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_assembler("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_of()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_of("test-input", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_input_exception()
    {
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_exception("test-input"));

        PipelineException exception = Assertions.assertThrows(PipelineException.class, () -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertTrue(exception.getCause() instanceof SinkWithExceptionException);
    }

    @Test
    public void testPipeline__shouldCompile_inputAndReturnValue()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_returnValue("test-input-return-value", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_payload()
    {
        StringCollector collector = new StringCollector();
        Pipeline<String> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_payload("test-payload", collector));

        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(output.payload(TestPayload.class).object().uid(), collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_output()
    {
        StringCollector collector = new StringCollector();
        Pipeline<String> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_output("test-output", collector));

        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(output.payload(TestPayload.class).object().uid(), collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_current()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_current("test-current", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->single(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_currentOptional()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentOptional("test-current-optional", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->optional(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_currentStream()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentStream("test-current-stream", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->stream(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_currentNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentNamed("test-current-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->single(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_currentOptionalNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentOptionalNamed("test-current-optional-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->optional(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_currentStreamNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentStreamNamed("test-current-stream-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->stream(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latest()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latest("test-latest", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->single(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latestOptional()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestOptional("test-latest-optional", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->optional(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latestStream()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestStream("test-latest-stream", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->stream(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latestNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestNamed("test-latest-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->single(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latestOptionalNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestOptionalNamed("test-latest-optional-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->optional(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_latestStreamNamed()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestStreamNamed("test-latest-stream-named", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input->stream(input)", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_tags()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_tags("test-tags", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("test-tags#sink-with_tags", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_results()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_results("test-results", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_context()
    {
        String key = "test_key";
        String value = "my_value";

        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_context("test-context", key, collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx.set(key, value)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(value, collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_contextKey()
    {
        List<String> collector = new ArrayList<>();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_contextKey("test-context-key", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx
            .set("some-metadata", "my_value")
            .set("some-primitive", 123)
        ));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(3, collector.size());
        Assertions.assertLinesMatch(List.of(
            "optional(my_value)",
            "primitive(123)",
            "single(my_value)"
        ), collector.stream().sorted().toList());
    }

    @Test
    public void testPipeline__shouldCompile_uidGenerator()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_uidGenerator("test-generator", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("KSUIDGenerator", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_observabilityManager()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_observabilityManager("observability-manager", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("SimpleMeterRegistry", collector.get());
    }

    @Test
    public void testPipeline__shouldCompile_markerManager()
    {
        StringCollector collector = new StringCollector();
        Pipeline<Object> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_markerManager("test-marker-manager", collector));

        Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("SinkMarkerManager", collector.get());
    }

    public static Pipeline<Object> createPipeline_input(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithInput<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_input_assembler(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(builder -> builder.sink(new SinkWithInput<>(collector)))
            .build();
    }

    public static Pipeline<Object> createPipeline_input_of(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(Sink.of(new SinkWithInput<>(collector)))
            .build();
    }

    public static Pipeline<Object> createPipeline_input_exception(String name)
    {
        return Pipeline.of(name)
            .registerSink(Sink.of(new SinkWithException<>()))
            .build();
    }

    public static Pipeline<Object> createPipeline_input_returnValue(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithInputAndReturnValue<>(collector))
            .build();
    }

    public static Pipeline<String> createPipeline_payload(String name, StringCollector collector)
    {
        return Pipeline.of(name, (Initializer<String>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer((SingleIndexer<TestPayload>) TestPayload::object)
            .registerSink(new SinkWithPayload(collector))
            .build();
    }

    public static Pipeline<String> createPipeline_output(String name, StringCollector collector)
    {
        return Pipeline.of(name, (Initializer<String>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer((SingleIndexer<TestPayload>) TestPayload::object)
            .registerSink(new SinkWithOutput(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_current(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrent<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_currentOptional(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrentOptional<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_currentStream(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndCurrentStream<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_currentNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndCurrent.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_currentOptionalNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndCurrentOptional.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_currentStreamNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndCurrentStream.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latest(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatest<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latestOptional(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatestOptional<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latestStream(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithInputAndLatestStream<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latestNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndLatest.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latestOptionalNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndLatestOptional.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_latestStreamNamed(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerSink(new SinkWithInputAndLatestStream.Named<>(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_tags(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithTags(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_results(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerSink(new SinkWithResults(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_context(String name, String metadataKey, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithContext(metadataKey, collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_contextKey(String name, List<String> collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithContextKey(collector))
            .registerSink(new SinkWithContextKeyOptional(collector))
            .registerSink(new SinkWithContextKeyPrimitive(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_uidGenerator(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithUIDGenerator(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_observabilityManager(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithObservabilityManager(collector))
            .build();
    }

    public static Pipeline<Object> createPipeline_markerManager(String name, StringCollector collector)
    {
        return Pipeline.of(name)
            .registerSink(new SinkWithMarkerManager(collector))
            .build();
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
