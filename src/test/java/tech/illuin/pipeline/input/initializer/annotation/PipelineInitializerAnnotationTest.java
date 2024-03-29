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
import tech.illuin.pipeline.sink.annotation.sink.SinkWithContextKey;
import tech.illuin.pipeline.sink.annotation.sink.SinkWithContextKeyOptional;
import tech.illuin.pipeline.sink.annotation.sink.SinkWithContextKeyPrimitive;

import java.util.List;

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
    public void testPipeline__shouldCompile_contextKey()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_contextKey("test-context-key"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx
            .set("some-metadata", "my_value")
        ));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("single(my_value)", output.payload().object().value());
    }

    @Test
    public void testPipeline__shouldCompile_contextKeyOptional()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_contextKeyOptional("test-context-key-optional"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx
            .set("some-metadata", "my_value")
        ));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("optional(my_value)", output.payload().object().value());
    }

    @Test
    public void testPipeline__shouldCompile_contextKeyPrimitive()
    {
        Pipeline<Object, TestPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipeline_contextKeyPrimitive("test-context-key-primitive"));

        Output<TestPayload> output = Assertions.assertDoesNotThrow(() -> pipeline.run("input", ctx -> ctx
            .set("some-primitive", 123)
        ));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("primitive(123)", output.payload().object().value());
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
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithInput<>())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_assembler(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(builder -> builder.initializer(new InitializerWithInput<>()))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_of(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(Initializer.of(new InitializerWithInput<>()))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_input_exception(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithException<>())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_tags(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithTags())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_context(String name, String metadataKey)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithContext(metadataKey))
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_contextKey(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithContextKey())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_contextKeyOptional(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithContextKeyOptional())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_contextKeyPrimitive(String name)
    {
        return Pipeline.of(name, TestPayload.class)
            .setInitializer(new InitializerWithContextKeyPrimitive())
            .registerIndexer(TestPayload::object)
            .build()
        ;
    }

    public static Pipeline<Object, TestPayload> createPipeline_logMarker(String name)
    {
        return Pipeline.of(name, TestPayload.class)
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
