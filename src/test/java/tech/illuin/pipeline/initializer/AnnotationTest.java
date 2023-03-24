package tech.illuin.pipeline.initializer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.initializer.TestAnnotatedInitializers;
import tech.illuin.pipeline.input.initializer.Initializer;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AnnotationTest
{
    @Test
    public void testPipeline_withAnnotatedInitializer()
    {
        var counter = new AtomicInteger(0);

        Pipeline<Void, ?> pipeline = Assertions.assertDoesNotThrow(() -> AnnotationTest.createAnnotatedPipeline(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    public static Pipeline<Void, A> createAnnotatedPipeline(AtomicInteger counter)
    {
        Initializer<Void, A> initializer = new TestAnnotatedInitializers.ErrorHandler<>("0", (gen, in) -> new A("a", Collections.emptyList()));
        return Pipeline.ofPayload("test-annotated", initializer)
            .registerSink(((output, context) -> {
                if (output.payload().uid().equals("a"))
                    counter.incrementAndGet();
            }))
            .build()
        ;
    }
}
