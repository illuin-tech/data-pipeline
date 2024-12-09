package tech.illuin.pipeline.step;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestMultiStep;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.step.execution.condition.MetadataCondition;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.result.Results;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tech.illuin.pipeline.generic.Tests.getResultTypes;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MultiResultTest
{
    @Test
    public void testPipeline_shouldRegisterMultiple()
    {
        Pipeline<?> pipeline = Assertions.assertDoesNotThrow(MultiResultTest::createMultiResultPipeline);
        Output output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        List<String> names = output.results().stream().map(Result::name).toList();
        List<String> statuses = output.results().stream().map(TestResult.class::cast).map(TestResult::status).toList();

        Assertions.assertIterableEquals(List.of("individual", "individual", "individual", "aggregate"), names);
        Assertions.assertIterableEquals(List.of("ok", "ko", "maybe", "ok+ko+maybe"), statuses);

        Assertions.assertTrue(output.results().current("individual").isPresent());
        Assertions.assertEquals(3, output.results().current().filter(r -> r.name().equals("individual")).count());
        Assertions.assertTrue(output.results().current("aggregate").isPresent());
        Assertions.assertEquals(1, output.results().current().filter(r -> r.name().equals("aggregate")).count());

    }

    private static Pipeline<?> createMultiResultPipeline()
    {
        return Pipeline.of("test-multi-result")
           .registerStep(builder -> builder
               .step(new TestMultiStep<>("individual", idx -> List.of("ok", "ko", "maybe")))
           )
           .registerStep(builder -> builder
               .step((InputStep<Object>) (input, results, context) -> {
                   String aggregate = results.current()
                       .filter(p -> p instanceof TestResult)
                       .map(TestResult.class::cast)
                       .map(TestResult::status)
                       .collect(Collectors.joining("+"));
                   return new TestResult("aggregate", aggregate);
               })
           )
           .build()
        ;
    }
}
