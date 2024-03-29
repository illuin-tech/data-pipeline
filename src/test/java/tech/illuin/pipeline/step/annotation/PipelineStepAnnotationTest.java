package tech.illuin.pipeline.step.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.step.*;
import tech.illuin.pipeline.step.annotation.step.StepWithException.StepWithExceptionException;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepAnnotationTest
{
    @Test
    public void testPipeline__shouldCompile_input()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input("test-input"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_input_assembler()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_assembler("test-input"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_input_of()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_of("test-input"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_input_exception()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_exception("test-input"));

        PipelineException exception = Assertions.assertThrows(PipelineException.class, () -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertTrue(exception.getCause() instanceof StepWithExceptionException);
    }

    @Test
    public void testPipeline__shouldCompile_object()
    {
        Pipeline<String, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_object("test-object"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            output.payload().object().uid(),
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_payload()
    {
        Pipeline<String, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_payload("test-payload"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            output.payload().object().uid(),
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_current()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_current("test-current"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input+input->single(input)+input->single(input)->optional(input)->stream(input)",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_currentNamed()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_currentNamed("test-current-named"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        List<String> statuses = output.results().stream(TestResult.class)
            .map(TestResult::status)
            .sorted()
            .toList()
        ;

        Assertions.assertEquals(4, statuses.size());
        Assertions.assertLinesMatch(List.of(
            "input",
            "input->optional(input)",
            "input->single(input)",
            "input->stream(input)"
        ), statuses);
    }

    @Test
    public void testPipeline__shouldCompile_latest()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latest("test-latest"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input+input->single(input)+input->single(input)->optional(input)->stream(input)",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_latestNamed()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latestNamed("test-latest-named"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        List<String> statuses = output.results().stream(TestResult.class)
            .map(TestResult::status)
            .sorted()
            .toList()
        ;

        Assertions.assertEquals(4, statuses.size());
        Assertions.assertLinesMatch(List.of(
            "input",
            "input->optional(input)",
            "input->single(input)",
            "input->stream(input)"
        ), statuses);
    }

    @Test
    public void testPipeline__shouldCompile_tags()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_tags("test-tags"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "test-tags#step-with_tags",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_results()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_results("test-results"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(2, output.results().stream(TestResult.class).count());
        Assertions.assertEquals(
            "input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_resultView()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_resultView("test-result_view"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(2, output.results().stream(TestResult.class).count());
        Assertions.assertEquals(
            "input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_context()
    {
        String key = "test_key";
        String value = "my_value";

        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_context("test-context", key));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx.set(key, value)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            value,
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_contextKey()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_contextKey("test-context-key"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx
            .set("some-metadata", "my_value")
            .set("some-primitive", 123)
        ));
        Assertions.assertDoesNotThrow(pipeline::close);

        List<String> statuses = output.results().stream(TestResult.class)
            .map(TestResult::status)
            .sorted()
            .toList()
        ;

        Assertions.assertEquals(3, statuses.size());
        Assertions.assertLinesMatch(List.of(
            "optional(my_value)",
            "primitive(123)",
            "single(my_value)"
        ), statuses);
    }

    @Test
    public void testPipeline__shouldCompile_uidGenerator()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_uidGenerator("test-uid-generator"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "KSUIDGenerator",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_logMarker()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_logMarker("test-log-marker"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "step-with_log-marker",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    public static Pipeline<Object, ?> createPipeline_input(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_assembler(String name)
    {
        return Pipeline.of(name)
            .registerStep(builder -> builder.step(new StepWithInput<>()))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_of(String name)
    {
        return Pipeline.of(name)
            .registerStep(Step.of(new StepWithInput<>()))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_exception(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithException<>())
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_object(String name)
    {
        return Pipeline.of(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerStep(new StepWithObject())
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_payload(String name)
    {
        return Pipeline.of(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerStep(new StepWithPayload())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_current(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithInputAndCurrent<>())
            .registerStep(new StepWithInputAndCurrentOptional<>())
            .registerStep(new StepWithInputAndCurrentStream<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_currentNamed(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerStep(new StepWithInputAndCurrent.Named<>())
            .registerStep(new StepWithInputAndCurrentOptional.Named<>())
            .registerStep(new StepWithInputAndCurrentStream.Named<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latest(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithInputAndLatest<>())
            .registerStep(new StepWithInputAndLatestOptional<>())
            .registerStep(new StepWithInputAndLatestStream<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latestNamed(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>("annotation-named"))
            .registerStep(new StepWithInputAndLatest.Named<>())
            .registerStep(new StepWithInputAndLatestOptional.Named<>())
            .registerStep(new StepWithInputAndLatestStream.Named<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_tags(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithTags())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_results(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithResults())
            .build()
        ;
    }


    public static Pipeline<Object, ?> createPipeline_resultView(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithResultView())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_context(String name, String metadataKey)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithContext(metadataKey))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_contextKey(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithContextKey())
            .registerStep(new StepWithContextKeyOptional())
            .registerStep(new StepWithContextKeyPrimitive())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_uidGenerator(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithUIDGenerator())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_logMarker(String name)
    {
        return Pipeline.of(name)
            .registerStep(new StepWithLogMarker())
            .build()
        ;
    }

    public record TestPayload(
        TestObject object
    ) {}

    public record TestObject(
        String uid
    ) implements Indexable {}
}
