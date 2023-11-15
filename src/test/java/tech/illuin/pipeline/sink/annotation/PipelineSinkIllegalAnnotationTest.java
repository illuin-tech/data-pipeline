package tech.illuin.pipeline.sink.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.sink.annotation.sink.*;
import tech.illuin.pipeline.step.annotation.step.StepWithInput;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineSinkIllegalAnnotationTest
{
    @Test
    public void testPipeline__noMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalSinkNoMethod()
        ));
    }

    @Test
    public void testPipeline__multipleMethods_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalSinkMultipleMethods<>()
        ));
    }

    @Test
    public void testPipeline__multipleParameterAnnotations_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalSinkMultipleAnnotations<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_object_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalSinkIllegalArgumentObject<>()
        ));
    }

    @Test
    public void testPipeline__illegalArgument_resultView_shouldNotCompile()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> createNonCompilingPipeline(
            "test-non-compiling",
            new IllegalSinkIllegalArgumentResultView()
        ));
    }

    public static Pipeline<Object, ?> createNonCompilingPipeline(String name, Object illegalSink)
    {
        return Pipeline.of(name)
           .registerStep(builder -> builder.step(new StepWithInput<>()))
           .registerSink(builder -> builder.sink(illegalSink))
           .build()
        ;
    }
}
