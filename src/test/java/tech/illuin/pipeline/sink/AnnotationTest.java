package tech.illuin.pipeline.sink;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.generic.pipeline.sink.TestAnnotatedSinks;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AnnotationTest
{
    @Test
    public void testPipeline_withAnnotatedSinks()
    {
        var counter = new AtomicInteger(0);

        Pipeline<Void> pipeline = Assertions.assertDoesNotThrow(() -> AnnotationTest.createAnnotatedPipeline(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    public static Pipeline<Void> createAnnotatedPipeline(AtomicInteger counter)
    {
        return Pipeline.<Void>of("test-annotated")
            .registerSink(new TestAnnotatedSinks.ErrorHandler<>("1", in -> { throw new RuntimeException("Some error"); }))
            .registerSink(new TestAnnotatedSinks.Async<>("2", in -> counter.incrementAndGet()))
            .build()
        ;
    }
}
