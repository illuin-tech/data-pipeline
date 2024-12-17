package tech.illuin.pipeline.observer;

import io.micrometer.core.instrument.MeterRegistry;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.List;

public interface Observer
{
    <I> void init(
        String id,
        InitializerDescriptor<I> initializer,
        List<StepDescriptor<Indexable, I>> steps,
        List<SinkDescriptor> sinks,
        PipelineErrorHandler errorHandler,
        List<OnCloseHandler> onCloseHandlers,
        MeterRegistry meterRegistry
    );

    default void close() throws Exception {}
}
