package tech.illuin.pipeline.sink;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public class SinkErrorHandlerTest
{
    @Test
    public void testPipeline_shouldHandleError()
    {
        AtomicInteger successfulSinkCounter = new AtomicInteger(0);
        AtomicInteger failedSinkCounter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createSinkErrorHandledPipeline(successfulSinkCounter, failedSinkCounter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(2, successfulSinkCounter.get());
        Assertions.assertEquals(1, failedSinkCounter.get());
    }

    @Test
    public void testPipeline_firstErrorHandlerFails_shouldHandleError()
    {
        AtomicInteger successfulSinkCounter = new AtomicInteger(0);
        AtomicInteger failedSinkCounter = new AtomicInteger(0);
        AtomicInteger failedSinkErrorHandlerCounter = new AtomicInteger(0);

        Pipeline<?, VoidPayload> pipeline = Assertions.assertDoesNotThrow(() -> createSinkErrorHandledPipelineWithTwoErrorHandlers(successfulSinkCounter, failedSinkCounter, failedSinkErrorHandlerCounter));
        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(2, successfulSinkCounter.get());
        Assertions.assertEquals(1, failedSinkCounter.get());
        Assertions.assertEquals(1, failedSinkErrorHandlerCounter.get());
    }

    public static Pipeline<?, VoidPayload> createSinkErrorHandledPipeline(AtomicInteger successfulSinkCounter, AtomicInteger failedSinkCounter)
    {
        return Pipeline.ofSimple("test-sink-sync-exception")
            .registerSink((o, ctx) -> successfulSinkCounter.incrementAndGet())
            .registerSink(builder -> builder
                .sink((o, ctx) -> { throw new Exception("Interrupted"); })
                .withErrorHandler((ex, output, ctx) -> failedSinkCounter.incrementAndGet())
            )
            .registerSink((o, ctx) -> successfulSinkCounter.incrementAndGet(), true)
            .build()
        ;
    }

    public static Pipeline<?, VoidPayload> createSinkErrorHandledPipelineWithTwoErrorHandlers(AtomicInteger successfulSinkCounter, AtomicInteger failedSinkCounter, AtomicInteger failedSinkErrorHandlerCounter)
    {
        SinkErrorHandler firstErrorHandler = (ex, output, ctx) -> {
            failedSinkErrorHandlerCounter.incrementAndGet();
            throw new Exception("Some error again");
        };
        SinkErrorHandler secondErrorHandler = (ex, output, ctx) -> failedSinkCounter.incrementAndGet();

        return Pipeline.ofSimple("test-sink-sync-exception")
            .registerSink((o, ctx) -> successfulSinkCounter.incrementAndGet())
            .registerSink(builder -> builder
                .sink((o, ctx) -> { throw new Exception("Interrupted"); })
                .withErrorHandler(firstErrorHandler.andThen(secondErrorHandler))
            )
            .registerSink((o, ctx) -> successfulSinkCounter.incrementAndGet(), true)
            .build()
        ;
    }
}
