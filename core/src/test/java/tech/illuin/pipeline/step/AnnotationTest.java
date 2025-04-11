package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.step.TestAnnotatedSteps;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.Output;

import java.util.List;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AnnotationTest
{
    @Test
    public void testPipeline_withAnnotatedSteps()
    {
        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(AnnotationTest::createAnnotatedPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "5", "error"), getResultTypes(output, output.payload(A.class)));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isEmpty());
        Assertions.assertTrue(output.results().current("3").isEmpty());
        Assertions.assertTrue(output.results().current("4").isEmpty());
        Assertions.assertTrue(output.results().current("error").isPresent());
        Assertions.assertTrue(output.results().current("5").isPresent());
    }

    public static Pipeline<Void> createAnnotatedPipeline()
    {
        return Pipeline.of("test-annotated", (Void input, LocalContext context, UIDGenerator generator) -> TestFactory.initializerOfEmpty(input, context, generator))
           .registerIndexer(SingleIndexer.auto())
           .registerStep(new TestAnnotatedSteps.ClassActivation<>("1", "ok"))
           .registerStep(new TestAnnotatedSteps.ConditionActivation<>("2", "ok"))
           .registerStep(new TestAnnotatedSteps.ErrorHandler<>("3", b -> { throw new RuntimeException("Some error"); }))
           .registerStep(new TestAnnotatedSteps.ClassActivation<>("4", "ok"))
           .registerStep(new TestAnnotatedSteps.Pinned<>("5", "ok"))
           .build()
        ;
    }
}
