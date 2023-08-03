package tech.illuin.pipeline.step.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.step.annotation.step.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineStepIllegalAnnotationTest
{
    @Test
    public void testPipeline__noMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalStepNoMethod()
        ));
    }

    @Test
    public void testPipeline__voidMethod_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalStepVoidMethod<>()
        ));
    }

    @Test
    public void testPipeline__multipleMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalStepMultipleMethods<>()
        ));
    }

    @Test
    public void testPipeline__multipleParameterAnnotations_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalStepMultipleAnnotations<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_object_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalStepIllegalArgumentOutput<>()
        ));
    }

    public static Pipeline<Object, ?> createNonCompilingPipeline(String name, Object illegalStep)
    {
        return Pipeline.ofSimple(name)
           .registerStep(builder -> builder.step(new StepWithInput<>()))
           .registerStep(builder -> builder.step(illegalStep))
           .build()
        ;
    }
}
