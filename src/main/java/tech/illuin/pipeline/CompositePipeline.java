package tech.illuin.pipeline;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.InitializerException;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineInitializationMetrics;
import tech.illuin.pipeline.metering.PipelineMetrics;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.PipelineStepMetrics;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.output.factory.OutputFactory;
import tech.illuin.pipeline.sink.SinkException;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultDescriptor;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static tech.illuin.pipeline.step.execution.evaluator.StrategyBehaviour.*;

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
    private final List<StepDescriptor<Indexable, I, P>> steps;
    private final List<SinkDescriptor<P>> sinks;

    /* Beware of using a ForkJoinPool, it does not play nice with the Spring ClassLoader ; if absolutely needed a custom ForkJoinWorkerThreadFactory would be required. */
    private final ExecutorService sinkExecutor;
    private final int closeTimeout;
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
        this.steps = steps;
        this.sinks = sinks;
        this.sinkExecutor = sinkExecutor;
        this.closeTimeout = closeTimeout;
        this.onCloseHandlers = onCloseHandlers;
        this.meterRegistry = meterRegistry;
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

        long start = System.nanoTime();
        try {
            logger.debug("{}: launching pipeline over input of type {}", this.id(), input != null ? input.getClass().getName() : "null");

            Output<P> output = this.runInitialization(input, context);
            this.runSteps(input, output, context);
            this.runSinks(output, context);

            logger.trace("{}#{} finished", this.id(), output.tag().uid());
            metrics.successCounter().increment();

            return output;
        }
        catch (InitializerException | StepException | SinkException e) {
            metrics.failureCounter().increment();
            throw new PipelineException(e.getMessage(), e);
        }
        catch (RuntimeException e) {
            metrics.failureCounter().increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Output<P> runInitialization(I input, Context<P> context) throws InitializerException
    {
        if (context == null)
            throw new IllegalArgumentException("Runtime context cannot be null");

        PipelineInitializationMetrics metrics = new PipelineInitializationMetrics(this.meterRegistry, this);
        long start = System.nanoTime();
        try {
            PipelineTag tag = new PipelineTag(this.uidGenerator.generate(), this.id(), this.authorResolver.resolve(input, context));

            P payload = this.runInitializer(input, tag, context);
            Output<P> output = this.outputFactory.create(tag, input, payload, context);

            for (Indexer<P> indexer : this.indexers)
            {
                logger.trace("{}#{} launching indexer {}", this.id(), tag.uid(), indexer.getClass().getName());
                indexer.index(payload, output.index());
            }

            metrics.successCounter().increment();
            return output;
        }
        catch (InitializerException | RuntimeException e) {
            metrics.failureCounter().increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private P runInitializer(I input, PipelineTag tag, Context<P> context) throws InitializerException
    {
        String name = getPrintableName(this.initializer);
        try {
            logger.trace("{}#{} initializing payload", this.id(), tag.uid());
            return this.initializer.execute(input, context, this.uidGenerator);
        }
        catch (InitializerException | RuntimeException e) {
            logger.trace("{}#{} initializer {} threw an {}: {}", this.id(), tag.uid(), name, e.getClass().getName(), e.getMessage());
            return this.initializer.handleException(e, context, this.uidGenerator);
        }
    }

    private void runSteps(I input, Output<P> output, Context<P> context) throws StepException
    {
        try {
            Set<Indexable> discarded = new HashSet<>();
            STEP_LOOP: for (StepDescriptor<Indexable, I, P> step : this.steps)
            {
                String name = getPrintableName(step);

                /* Arguments are a list of Indexable which satisfy the step's execution predicate */
                List<Indexable> arguments = output.index().stream()
                    .filter(idx -> !discarded.contains(idx) || step.isPinned())
                    .filter(idx -> step.canExecute(idx, context))
                    .toList()
                ;

                logger.trace("{}#{} retrieved {} arguments for step {}", this.id(), output.tag().uid(), arguments.size(), name);

                /* For each argument we perform the step and register the produced Result */
                for (Indexable indexed : arguments)
                {
                    Result result = this.runStep(step, indexed, input, output, context);

                    StepStrategy strategy = step.postEvaluation(result);
                    logger.trace("{}#{} received {} signal after step {} over argument {}", this.id(), output.tag().uid(), strategy, name, indexed.uid());

                    if (strategy.hasBehaviour(REGISTER_RESULT))
                    {
                        if (result instanceof PipelineResult<?> pResult)
                            pResult.output().results().descriptors().current().forEach(rd -> output.results().register(indexed.uid(), rd));
                        else {
                            output.results().register(indexed.uid(), new ResultDescriptor<>(
                                this.uidGenerator.generate(),
                                output.tag(),
                                Instant.now(),
                                result
                            ));
                        }

                    }
                    if (strategy.hasBehaviour(DISCARD_CURRENT))
                        discarded.add(indexed);
                    if (strategy.hasBehaviour(DISCARD_ALL))
                        discarded.addAll(output.index().stream().toList());
                    if (strategy.hasBehaviour(STOP_CURRENT))
                        break;
                    if (strategy.hasBehaviour(STOP_ALL))
                        break STEP_LOOP;
                }
            }
        }
        finally {
            output.finish();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result runStep(StepDescriptor<Indexable, I, P> step, Indexable indexed, I input, Output<P> output, Context<P> context) throws StepException
    {
        String name = getPrintableName(step);
        PipelineStepMetrics metrics = new PipelineStepMetrics(this.meterRegistry, this, step);

        long start = System.nanoTime();
        try {
            logger.trace("{}#{} running step {} over argument {}", this.id(), output.tag().uid(), name, indexed.uid());
            Result result = step.execute(indexed, input, output);

            metrics.successCounter().increment();
            return result;
        }
        catch (StepException | RuntimeException e) {
            logger.trace("{}#{} step {} threw an {}: {}", this.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            return step.handleException(e, input, output.payload(), output.results(), context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    private void runSinks(Output<P> output, Context<P> context) throws SinkException
    {
        for (SinkDescriptor<P> descriptor : this.sinks)
        {
            PipelineSinkMetrics metrics = new PipelineSinkMetrics(this.meterRegistry, this, descriptor);

            if (descriptor.isAsync())
                this.runSinkAsynchronously(descriptor, output, context, metrics);
            else
                this.runSinkSynchronously(descriptor, output, context, metrics);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkSynchronously(SinkDescriptor<P> sink, Output<P> output, Context<P> context, PipelineSinkMetrics metrics) throws SinkException
    {
        String name = getPrintableName(sink);
        long start = System.nanoTime();
        try {
            logger.trace("{}#{} launching sink {}", this.id(), output.tag().uid(), name);
            sink.execute(output);
            metrics.successCounter().increment();
        }
        catch (SinkException | RuntimeException e) {
            logger.error("{}#{} sink {} threw an {}: {}", this.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            sink.handleException(e, output, context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor<P> sink, Output<P> output, Context<P> context, PipelineSinkMetrics metrics)
    {
        String name = getPrintableName(sink);
        logger.trace("{}#{} queuing sink {}", this.id(), output.tag().uid(), name);
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            try {
                logger.trace("{}#{} launching sink {}", this.id(), output.tag().uid(), name);
                sink.execute(output);
                metrics.successCounter().increment();
            }
            catch (SinkException | RuntimeException e) {
                logger.error("{}#{} sink {} threw an {}: {}", this.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
                metrics.failureCounter().increment();
                sink.handleExceptionThenSwallow(e, output, context);
            }
            finally {
                metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                metrics.totalCounter().increment();
            }
        }, this.sinkExecutor);
    }

    @Override
    public synchronized void close() throws Exception
    {
        if (this.sinkExecutor.isShutdown())
            return;

        logger.trace("{} closing, launching {} on-close handlers", this.id(), this.onCloseHandlers.size());
        for (OnCloseHandler handler : this.onCloseHandlers)
            handler.execute();

        this.sinkExecutor.shutdown();
        boolean success = this.sinkExecutor.awaitTermination(this.closeTimeout, TimeUnit.SECONDS);
        var status = success ? "done" : "timeout after " + this.closeTimeout + " seconds";

        logger.info("{} closed (executor termination status: {})", this.id(), status);
    }

    private static String getPrintableName(InitializerDescriptor<?, ?> initializer)
    {
        return initializer.id();
    }

    private static String getPrintableName(StepDescriptor<?, ?, ?> step)
    {
        return step.id() + (step.isPinned() ? " (pinned)" : "");
    }

    private static String getPrintableName(SinkDescriptor<?> sink)
    {
        return sink.id() + (sink.isAsync() ? " (async)" : "");
    }
}
