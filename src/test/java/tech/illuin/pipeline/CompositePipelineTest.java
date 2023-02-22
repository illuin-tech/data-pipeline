package tech.illuin.pipeline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.generic.TestFactory;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CompositePipelineTest
{
    @Test
    public void testPipeline_shouldAbort()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(TestFactory::createAbortingPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "4"), getResultTypes(output, output.payload()));
        Assertions.assertIterableEquals(List.of("2", "3"), getResultTypes(output, output.payload().bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload().bs().get(1)));

        Assertions.assertEquals(3, output.results().size());
        Assertions.assertEquals(5, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
        Assertions.assertTrue(output.results().current("4").isPresent());
        Assertions.assertTrue(output.results().current("5").isEmpty());
    }

    @Test
    public void testPipeline_shouldHandleError()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(TestFactory::createErrorHandledPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "error"), getResultTypes(output, output.payload()));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload().bs().get(0)));
        Assertions.assertIterableEquals(List.of("2"), getResultTypes(output, output.payload().bs().get(1)));

        Assertions.assertEquals(3, output.results().size());
        Assertions.assertEquals(4, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isEmpty());
        Assertions.assertTrue(output.results().current("4").isEmpty());
        Assertions.assertTrue(output.results().current("error").isPresent());
    }

    @Test
    public void testPipeline_withAnnotatedSteps()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(TestFactory::createAnnotatedPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "error"), getResultTypes(output, output.payload()));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(2, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isEmpty());
        Assertions.assertTrue(output.results().current("3").isEmpty());
        Assertions.assertTrue(output.results().current("4").isEmpty());
        Assertions.assertTrue(output.results().current("error").isPresent());
    }

    @Test
    public void testPipeline_shouldThrowException()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(TestFactory::createErrorThrownPipeline);
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, pipeline::run);
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals("Some error", exception.getMessage());
    }

    @Test
    public void testPipeline_shouldRetryException()
    {
        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(TestFactory::createErrorRetryPipeline);
        Output<A> output = Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertIterableEquals(List.of("1", "2", "3"), getResultTypes(output, output.payload()));

        Assertions.assertEquals(1, output.results().size());
        Assertions.assertEquals(3, output.results().current().count());

        Assertions.assertTrue(output.results().current("1").isPresent());
        Assertions.assertTrue(output.results().current("2").isPresent());
        Assertions.assertTrue(output.results().current("3").isPresent());
    }

    @Test
    public void testPipeline_shouldLaunchSinks()
    {
        var counter = new AtomicInteger(0);

        Pipeline<Void, A> pipeline = Assertions.assertDoesNotThrow(() -> TestFactory.createPipelineWithSinks(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(4, counter.get());
    }

    private static List<String> getResultTypes(Output<?> output, Indexable object)
    {
        return output.results().stream(object).map(Result::type).sorted().toList();
    }
}
