package tech.illuin.pipeline;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.execution.phase.IO;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.execution.phase.impl.InitializerPhase;
import tech.illuin.pipeline.execution.phase.impl.SinkPhase;
import tech.illuin.pipeline.execution.phase.impl.StepPhase;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineMetrics;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.metering.tag.TagResolver;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.observer.descriptor.DescriptorObserver;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.output.factory.OutputFactory;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>The CompositePipeline works by separating its run implementation into predefined categories of components:</p>
 * <ul>
 *     <li>1 {@link tech.illuin.pipeline.input.initializer.Initializer} responsible for creating the pipeline payload, which is reference in the {@link tech.illuin.pipeline.output.Output} and made available to steps and sinks</li>
 *     <li>1 to n {@link tech.illuin.pipeline.input.indexer.Indexer} responsible for identifying parts (or all) of the payload that will be subjected to steps</li>
 *     <li>0 to n {@link tech.illuin.pipeline.step.Step} performing transformative operations, ideally without external side effects, their output is indexed in the {@link tech.illuin.pipeline.step.result.ResultContainer}</li>
 *     <li>0 to n {@link tech.illuin.pipeline.sink.Sink} performing terminal operations, expected to be external side effects, they can be executed in a synchronous or asynchronous fashion</li>
 *     <li>0 to 1 {@link tech.illuin.pipeline.execution.error.PipelineErrorHandler} responsible for handling and possibly recovering from exceptions occurring during the pipeline's execution</li>
 *     <li>0 to n {@link tech.illuin.pipeline.close.OnCloseHandler} responsible for cleaning up when tearing down the pipeline</li>
 * </ul>
 * <p>The pipeline references an {@link java.util.concurrent.ExecutorService} which the pipeline will attempt to close when its own close() method is called.</p>
 * <p>It will additionally handle a variety of {@link MeterRegistry} counters reflecting the activity of its components.</p>
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class CompositePipeline<I> implements Pipeline<I>
{
    private final String id;
    private final UIDGenerator uidGenerator;
    private final AuthorResolver<I> authorResolver;
    private final List<PipelinePhase<I>> phases;
    private final PipelineErrorHandler errorHandler;
    private final List<OnCloseHandler> onCloseHandlers;
    private final MeterRegistry meterRegistry;
    private final TagResolver<I> tagResolver;
    private final DescriptorObserver descriptorObserver;
    private final List<Observer> observers;

    private static final Logger logger = LoggerFactory.getLogger(CompositePipeline.class);

    @SuppressWarnings("ParameterNumber")
    public CompositePipeline(
        String id,
        UIDGenerator uidGenerator,
        InitializerDescriptor<I> initializer,
        AuthorResolver<I> authorResolver,
        List<Indexer<?>> indexers,
        OutputFactory<I> outputFactory,
        List<StepDescriptor<Indexable, I>> steps,
        List<SinkDescriptor> sinks,
        Supplier<ExecutorService> sinkExecutorProvider,
        PipelineErrorHandler errorHandler,
        int closeTimeout,
        List<OnCloseHandler> onCloseHandlers,
        MeterRegistry meterRegistry,
        TagResolver<I> tagResolver,
        List<Observer> observers
    ) {
        this.id = id;
        this.uidGenerator = uidGenerator;
        this.authorResolver = authorResolver;
        this.errorHandler = errorHandler;
        this.onCloseHandlers = onCloseHandlers;
        this.meterRegistry = meterRegistry;
        this.tagResolver = tagResolver;
        this.descriptorObserver = new DescriptorObserver();
        this.observers = Stream.concat(
            Stream.of(this.descriptorObserver),
            observers.stream()
        ).toList();
        this.observers.forEach(s -> s.init(
            id, initializer, steps, sinks, errorHandler, onCloseHandlers, meterRegistry
        ));
        this.phases = List.of(
            new InitializerPhase<>(initializer, tagResolver, indexers, outputFactory, uidGenerator, meterRegistry),
            new StepPhase<>(steps, uidGenerator, meterRegistry),
            new SinkPhase<>(id, sinks, sinkExecutorProvider, closeTimeout, uidGenerator, meterRegistry)
        );
    }

    @Override
    public String id()
    {
        return this.id;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Output run(I input, Context context) throws PipelineException
    {
        PipelineTag tag = this.createTag(input, context);
        MetricTags metricTags = this.tagResolver.resolve(input, context);
        PipelineMetrics metrics = new PipelineMetrics(this.meterRegistry, tag, metricTags);
        IO<I> io = new IO<>(tag, input);

        long start = System.nanoTime();
        metrics.setMDC();
        try {
            logger.debug("{}: launching pipeline over input of type {}", this.id(), input != null ? input.getClass().getName() : "null");

            /* We will go iteratively through each phase, if one requires a pipeline exit we will skip remaining phases */
            for (PipelinePhase<I> phase : this.phases)
            {
                PipelineStrategy strategy = phase.run(io, context, metricTags);
                if (strategy == PipelineStrategy.EXIT)
                    break;
            }

            logger.trace("{}#{} finished", this.id(), io.output().tag().uid());
            metrics.successCounter().increment();

            return io.output();
        }
        catch (Exception e) {
            metrics.setMDC(e);
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            logger.error("{}: {}", this.id(), e.getMessage());
            return this.errorHandler.handle(e, io.output(), input, context, tag);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
        }
    }

    @Override
    public synchronized void close() throws Exception
    {
        logger.trace("{} closing, launching {} on-close handlers", this.id(), this.onCloseHandlers.size());
        for (OnCloseHandler handler : this.onCloseHandlers)
            handler.execute();

        logger.trace("{} closing, closing {} phases", this.id(), this.phases.size());
        for (PipelinePhase<I> phase : this.phases)
            phase.close();

        logger.trace("{} closing, closing {} observers", this.id(), this.observers.size());
        for (Observer observer : this.observers)
            observer.close();
    }

    private PipelineTag createTag(I input, Context context)
    {
        return new PipelineTag(this.uidGenerator.generate(), this.id(), this.authorResolver.resolve(input, context));
    }

    @Override
    public PipelineDescription describe()
    {
        return this.descriptorObserver.describe();
    }
}
