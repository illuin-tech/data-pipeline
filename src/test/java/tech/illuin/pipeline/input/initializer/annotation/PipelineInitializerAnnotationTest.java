package tech.illuin.pipeline.input.initializer.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.annotation.initializer.InitializerWithContext;
import tech.illuin.pipeline.input.initializer.annotation.initializer.InitializerWithInput;
import tech.illuin.pipeline.input.initializer.annotation.initializer.InitializerWithTags;
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

    public static Pipeline<Object, TestPayload> createPipeline_input(String name)
    {
        return Pipeline.<Object, TestPayload>ofPayload(name)
            .setInitializer(new InitializerWithInput<>())
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

    public record TestPayload(
        TestObject object
    ) {}

    public record TestObject(
        String uid,
        String value
    ) implements Indexable {}
}
