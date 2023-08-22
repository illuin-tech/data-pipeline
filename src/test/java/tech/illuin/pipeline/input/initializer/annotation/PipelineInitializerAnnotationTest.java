package tech.illuin.pipeline.input.initializer.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.initializer.*;
import tech.illuin.pipeline.input.initializer.annotation.initializer.InitializerWithException.InitializerWithExceptionException;
import tech.illuin.pipeline.output.Output;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializerAnnotationTest
{
    @Test
    public void testPipeline__shouldCompile_input()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input("test-input"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", output.payload().object().value());
    }

    @Test
    public void testPipeline__shouldCompile_input_assembler()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_assembler("test-input"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", output.payload().object().value());
    }

    @Test
    public void testPipeline__shouldCompile_input_of()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_of("test-input"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("input", output.payload().object().value());
    }


    @Test
    public void testPipeline__shouldCompile_input_exception()
    {
        Pipeline<Object, ?> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_input_exception("test-input"));

        PipelineException exception = Assertions.assertThrows(PipelineException.class, () -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertTrue(exception.getCause() instanceof InitializerWithExceptionException);
    }

    @Test
    public void testPipeline__shouldCompile_tags()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_tags("test-tags"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "test-tags#init-with_tags",
            output.payload().object().value()
        );
    }

    @Test
    public void testPipeline__shouldCompile_context()
    {
        String key = "test_key";
        String value = "my_value";

        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_context("test-context", key));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx.set(key, value)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(value, output.payload().object().value());
    }

    @Test
    public void testPipeline__shouldCompile_logMarker()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_logMarker("test-log-marker"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(
            "init-with_log-marker",
            output.payload().object().value()
        );
    }

    public static Pipeline<Object, TestPayload> createPipeline_input(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithInput<>())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_assembler(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(builder -> builder.initializer(new InitializerWithInput<>()))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_of(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(Initializer.of(new InitializerWithInput<>()))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_exception(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithException<>())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_tags(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithTags())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_context(String name, String metadataKey)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithContext(metadataKey))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_logMarker(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithLogMarker())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public record TestPayload(
        TestObject object
    ) {}

    public record TestObject(
        String uid,
        String value
    ) implements Indexable {}
}
