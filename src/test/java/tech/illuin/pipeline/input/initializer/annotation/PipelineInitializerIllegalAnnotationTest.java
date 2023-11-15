package tech.illuin.pipeline.input.initializer.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.input.initializer.annotation.initializer.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineInitializerIllegalAnnotationTest
{
    @Test
    public void testPipeline__noMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerNoMethod()
        ));
    }

    @Test
    public void testPipeline__voidMethod_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerVoidMethod<>()
        ));
    }

    @Test
    public void testPipeline__multipleMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerMultipleMethods<>()
        ));
    }

    @Test
    public void testPipeline__multipleParameterAnnotations_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerMultipleAnnotations<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_object_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerIllegalArgumentObject<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_payload_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerIllegalArgumentPayload<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_output_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerIllegalArgumentOutput<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_results_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerIllegalArgumentResults()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_resultView_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalInitializerIllegalArgumentResultView()
        ));
    }

    public static Pipeline<Object, ?> createNonCompilingPipeline(String name, Object illegalInitializer)
    {
        return Pipeline.of(name, Object.class)
            .setInitializer(illegalInitializer)
            .build()
        ;
    }
}
