package tech.illuin.pipeline;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.execution.phase.impl.SinkPhase;
import tech.illuin.pipeline.execution.phase.impl.StepPhase;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineInitializationMetrics;
import tech.illuin.pipeline.metering.PipelineMetrics;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.output.factory.OutputFactory;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.builder.StepDescriptor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>The CompositePipeline works by separating its run implementation into predefined categories of components:</p>
 * <ul>
 *     <li>1 {@link tech.illuin.pipeline.input.initializer.Initializer} responsible for creating the pipeline payload, which is reference in the {@link tech.illuin.pipeline.output.Output} and made available to steps and sinks</li>
 *     <li>1 to n {@link tech.illuin.pipeline.input.indexer.Indexer} responsible for identifying parts (or all) of the payload that will be subjected to steps</li>
 *     <li>0 to n {@link tech.illuin.pipeline.step.Step} performing transformative operations, ideally without external side effects, their output is indexed in the {@link tech.illuin.pipeline.step.result.ResultContainer}</li>
 *     <li>0 to n {@link tech.illuin.pipeline.sink.Sink} performing terminal operations, expected to be external side effects, they can be executed in a synchronous or asynchronous fashion</li>
 *     <li>0 to n {@link tech.illuin.pipeline.close.OnCloseHandler} responsible for cleaning up when tearing down the pipeline</li>
 * </ul>
 * <p>The pipeline references an {@link java.util.concurrent.ExecutorService} which the pipeline will attempt to close when its own close() method is called.</p>
 * <p>It will additionally handle a variety of {@link MeterRegistry} counters reflecting the activity of its components.</p>
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class CompositePipeline<I, P> implements Pipeline<I, P>
{
    private final String id;
    private final UIDGenerator uidGenerator;
    private final InitializerDescriptor<I, P> initializer;
    private final AuthorResolver<I> authorResolver;
    private final List<Indexer<P>> indexers;
    private final OutputFactory<I, P> outputFactory;
    private final List<PipelinePhase<I, P>> phases;
    private final List<OnCloseHandler> onCloseHandlers;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(CompositePipeline.class);

    @SuppressWarnings("ParameterNumber")
    public CompositePipeline(
        String id,
        UIDGenerator uidGenerator,
        InitializerDescriptor<I, P> initializer,
        AuthorResolver<I> authorResolver,
        List<Indexer<P>> indexers,
        OutputFactory<I, P> outputFactory,
        List<StepDescriptor<Indexable, I, P>> steps,
        List<SinkDescriptor<P>> sinks,
        ExecutorService sinkExecutor,
        int closeTimeout,
        List<OnCloseHandler> onCloseHandlers,
        MeterRegistry meterRegistry
    ) {
        this.id = id;
        this.uidGenerator = uidGenerator;
        this.initializer = initializer;
        this.authorResolver = authorResolver;
        this.indexers = indexers;
        this.outputFactory = outputFactory;
        this.onCloseHandlers = onCloseHandlers;
        this.meterRegistry = meterRegistry;
        this.phases = List.of(
            new StepPhase<>(this, steps, uidGenerator, meterRegistry),
            new SinkPhase<>(this, sinks, sinkExecutor, closeTimeout, meterRegistry)
        );
    }

    @Override
    public String id()
    {
        return this.id;
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public Output<P> run(I input, Context<P> context) throws PipelineException
    {
        PipelineMetrics metrics = new PipelineMetrics(this.meterRegistry, this);
        Output<P> output = null;

        long start = System.nanoTime();
        try {
            logger.debug(metrics.marker(), "{}: launching pipeline over input of type {}", this.id(), input != null ? input.getClass().getName() : "null");

            output = this.runInitialization(input, context);

            /* We will go iteratively through each phase, if one requires a pipeline exit we will skip remaining phases */
            for (PipelinePhase<I, P> phase : this.phases)
            {
                PipelineStrategy strategy = phase.run(input, output, context);
                if (strategy == PipelineStrategy.EXIT)
                    break;
            }

            logger.trace(metrics.marker(output.tag()), "{}#{} finished", this.id(), output.tag().uid());
            metrics.successCounter().increment();

            return output;
        }
        catch (Exception e) {
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            LabelMarker marker = output == null ? metrics.marker(e) : metrics.marker(output.tag(), e);
            logger.error(marker, "{}: {}", this.id(), e.getMessage());
            if (e instanceof RuntimeException re)
                throw re;
            throw new PipelineException(e.getMessage(), e);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Output<P> runInitialization(I input, Context<P> context) throws Exception
    {
        if (context == null)
            throw new IllegalArgumentException("Runtime context cannot be null");

        PipelineInitializationMetrics metrics = new PipelineInitializationMetrics(this.meterRegistry, this, this.initializer);
        long start = System.nanoTime();
        try {
            PipelineTag tag = new PipelineTag(this.uidGenerator.generate(), this.id(), this.authorResolver.resolve(input, context));

            P payload = this.runInitializer(input, tag, context, metrics);
            Output<P> output = this.outputFactory.create(tag, input, payload, context);

            for (Indexer<P> indexer : this.indexers)
            {
                logger.trace(metrics.marker(tag), "{}#{} launching indexer {}", this.id(), tag.uid(), indexer.getClass().getName());
                indexer.index(payload, output.index());
            }

            metrics.successCounter().increment();
            return output;
        }
        catch (Exception e) {
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private P runInitializer(I input, PipelineTag tag, Context<P> context, PipelineInitializationMetrics metrics) throws Exception
    {
        String name = getPrintableName(this.initializer);
        try {
            logger.trace(metrics.marker(tag), "{}#{} initializing payload", this.id(), tag.uid());
            return this.initializer.execute(input, context, this.uidGenerator);
        }
        catch (Exception e) {
            logger.trace(metrics.marker(tag, e), "{}#{} initializer {} threw an {}: {}", this.id(), tag.uid(), name, e.getClass().getName(), e.getMessage());
            return this.initializer.handleException(e, context, this.uidGenerator);
        }
    }

    @Override
    public synchronized void close() throws Exception
    {
        logger.trace("{} closing, launching {} on-close handlers", this.id(), this.onCloseHandlers.size());
        for (OnCloseHandler handler : this.onCloseHandlers)
            handler.execute();

        logger.trace("{} closing, closing {} phases", this.id(), this.phases.size());
        for (PipelinePhase<I, P> phase : this.phases)
            phase.close();
    }

    private static String getPrintableName(InitializerDescriptor<?, ?> initializer)
    {
        return initializer.id();
    }
}
