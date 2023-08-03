package tech.illuin.pipeline.step.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.step.*;

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
            "input+input->input+input->input->input->input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
    }

    @Test
    public void testPipeline__shouldCompile_latest()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_latest("test-latest"));

        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "input+input->input+input->input->input->input",
            output.results().current(TestResult.class).map(TestResult::status).orElse(null)
        );
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

    public static Pipeline<Object, ?> createPipeline_input(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_assembler(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(builder -> builder.step(new StepWithInput<>()))
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_input_of(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(Step.of(new StepWithInput<>()))
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_object(String name)
    {
        return Pipeline.ofPayload(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerStep(new StepWithObject())
            .build()
        ;
    }

    public static Pipeline<String, TestPayload> createPipeline_payload(String name)
    {
        return Pipeline.ofPayload(name, (Initializer<String, TestPayload>) (input, context, generator) -> new TestPayload(new TestObject(generator.generate())))
            .registerIndexer(TestPayload::object)
            .registerStep(new StepWithPayload())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_current(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithInputAndCurrent<>())
            .registerStep(new StepWithInputAndCurrentOptional<>())
            .registerStep(new StepWithInputAndCurrentStream<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_latest(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithInputAndLatest<>())
            .registerStep(new StepWithInputAndLatestOptional<>())
            .registerStep(new StepWithInputAndLatestStream<>())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_tags(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithTags())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_results(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithResults())
            .build()
        ;
    }


    public static Pipeline<Object, ?> createPipeline_resultView(String name)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithInput<>())
            .registerStep(new StepWithResultView())
            .build()
        ;
    }

    public static Pipeline<Object, ?> createPipeline_context(String name, String metadataKey)
    {
        return Pipeline.ofSimple(name)
            .registerStep(new StepWithContext(metadataKey))
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
