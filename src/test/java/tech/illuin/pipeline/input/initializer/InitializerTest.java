package tech.illuin.pipeline.input.initializer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.input.indexer.SingleIndexer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerTest
{
    @Test
    public void testPipeline_shouldInitialize()
    {
        var counter = new AtomicInteger(0);

        Pipeline<String, B> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithInitializer(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run("my_input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testPipeline_shouldFail()
    {
        var counter = new AtomicInteger(0);

        Pipeline<String, B> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithInitializer_exception(counter));
        PipelineException ex = Assertions.assertThrows(PipelineException.class, () -> pipeline.run("my_input"));
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(0, counter.get());
        Assertions.assertEquals("my_input", ex.getMessage());
    }

    public static Pipeline<String, B> createPipelineWithInitializer(AtomicInteger counter)
    {
        Initializer<String, B> init = (input, context, generator) -> new B(generator.generate(), input);
        return Pipeline.ofPayload("test-init", init)
            .registerIndexer(SingleIndexer.auto())
            .registerSink((o, ctx) -> {
                if (o.payload().name().equals("my_input"))
                    counter.incrementAndGet();
            })
            .build()
        ;
    }

    public static Pipeline<String, B> createPipelineWithInitializer_exception(AtomicInteger counter)
    {
        Initializer<String, B> init = (input, context, generator) -> { throw new Exception(input); };
        return Pipeline.ofPayload("test-init-error", init)
            .registerIndexer(SingleIndexer.auto())
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .build()
        ;
    }
}
