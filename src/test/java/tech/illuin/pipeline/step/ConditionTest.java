package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.execution.condition.MetadataCondition;

import java.util.List;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ConditionTest
{
    @Test
    public void testPipeline_shouldMatchEnabled()
    {
        Pipeline<?, ?> pipeline = Assertions.assertDoesNotThrow(ConditionTest::createConditionalPipeline);
        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx -> ctx.set("enabled", true)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "3"), getResultTypes(output));

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    @Test
    public void testPipeline_shouldMatchDisabled()
    {
        Pipeline<?, ?> pipeline = Assertions.assertDoesNotThrow(ConditionTest::createConditionalPipeline);
        Output<?> output = Assertions.assertDoesNotThrow(() -> pipeline.run(null, ctx -> ctx.set("enabled", false)));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("2", "3"), getResultTypes(output));

        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    public static Pipeline<?, ?> createConditionalPipeline()
    {
        return Pipeline.of("test-condition")
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withCondition(new MetadataCondition("enabled", true))
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("2", "ok"))
               .withCondition(new MetadataCondition("enabled", false))
           )
           .registerStep(new TestStep<>("3", "ok"))
           .build()
        ;
    }
}
