package tech.illuin.pipeline.sink;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.builder.VoidPayload;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkTest
{
    @Test
    public void testPipeline_shouldLaunchSinks()
    {
        var counter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithSinks(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(4, counter.get());
    }

    @Test
    public void testPipeline_shouldLaunchSinks__withSyncException()
    {
        var counter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithSinks_syncException(counter));
        PipelineException ex = Assertions.assertThrows(PipelineException.class, pipeline::run);
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
        Assertions.assertTrue(ex.getCause() instanceof SinkException);
    }

    @Test
    public void testPipeline_shouldLaunchSinks__withASyncException()
    {
        var counter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createPipelineWithSinks_asyncException(counter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(3, counter.get());
    }

    public static Pipeline<?, VoidPayload> createPipelineWithSinks(AtomicInteger counter)
    {
        return Pipeline.ofSimple("test-sink")
           .registerSink((o, ctx) -> counter.incrementAndGet())
           .registerSink((o, ctx) -> counter.incrementAndGet())
           .registerSink((o, ctx) -> counter.incrementAndGet(), true)
           .registerSink((o, ctx) -> counter.incrementAndGet(), true)
           .build()
        ;
    }

    public static Pipeline<?, VoidPayload> createPipelineWithSinks_syncException(AtomicInteger counter)
    {
        return Pipeline.ofSimple("test-sink-sync-exception")
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .registerSink((o, ctx) -> { throw new SinkException("Interrupted"); })
            .registerSink((o, ctx) -> counter.incrementAndGet(), true)
            .build()
        ;
    }

    public static Pipeline<?, VoidPayload> createPipelineWithSinks_asyncException(AtomicInteger counter)
    {
        return Pipeline.ofSimple("test-sink-async-exception")
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .registerSink((o, ctx) -> { throw new SinkException("Interrupted"); }, true)
            .registerSink((o, ctx) -> counter.incrementAndGet(), true)
            .build()
        ;
    }
}
