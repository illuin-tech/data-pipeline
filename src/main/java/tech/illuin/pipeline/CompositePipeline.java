package tech.illuin.pipeline;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.metering.PipelineInitializationMetrics;
import tech.illuin.pipeline.metering.PipelineMetrics;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.PipelineStepMetrics;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.SinkDescriptor;
import tech.illuin.pipeline.sink.SinkException;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class CompositePipeline<I, P> implements Pipeline<I, P>
{
    private final String id;
    private final Initializer<I, P> initializer;
    private final AuthorResolver<I> authorResolver;
    private final List<Indexer<P>> indexers;
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
        Initializer<I, P> initializer,
        AuthorResolver<I> authorResolver,
        List<Indexer<P>> indexers,
        List<StepDescriptor<Indexable, I, P>> steps,
        List<SinkDescriptor<P>> sinks,
        ExecutorService sinkExecutor,
        int closeTimeout,
        List<OnCloseHandler> onCloseHandlers,
        MeterRegistry meterRegistry
    ) {
        this.id = id;
        this.initializer = initializer;
        this.authorResolver = authorResolver;
        this.indexers = indexers;
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
        catch (StepException | SinkException e) {
            metrics.failureCounter().increment();
            throw new PipelineException(e);
        }
        catch (Exception e) {
            metrics.failureCounter().increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Output<P> runInitialization(I input, Context<P> context)
    {
        if (context == null)
            throw new IllegalArgumentException("Runtime context cannot be null");

        PipelineInitializationMetrics metrics = new PipelineInitializationMetrics(this.meterRegistry, this);

        long start = System.nanoTime();
        try {
            Output<P> output = new Output<>(
                this.id(),
                this.authorResolver.resolve(input, context)
            );

            logger.trace("{}#{} initializing payload", this.id(), output.tag().uid());
            output.setPayload(this.initializer.initialize(input, context));

            for (Indexer<P> indexer : this.indexers)
            {
                logger.trace("{}#{} launching indexer {}", this.id(), output.tag().uid(), indexer.getClass().getName());
                indexer.index(output.payload(), output.index());
            }

            context.parent().ifPresent(parent -> output.results().register(parent.results()));

            metrics.successCounter().increment();

            return output;
        }
        catch (Exception e) {
            metrics.failureCounter().increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    private void runSteps(I input, Output<P> output, Context<P> context) throws StepException
    {
        try {
            Set<Indexable> discarded = new HashSet<>();
            STEP_LOOP: for (StepDescriptor<Indexable, I, P> step : this.steps)
            {
                /* Arguments are a list of Indexable which satisfy the step's execution predicate */
                List<Indexable> arguments = output.index().stream()
                    .filter(idx -> !discarded.contains(idx))
                    .filter(step::canExecute)
                    .toList()
                ;

                logger.trace("{}#{} retrieved {} arguments for step {}", this.id(), output.tag().uid(), arguments.size(), step.id());

                /* For each argument we perform the step and register the produced Result */
                for (Indexable indexed : arguments)
                {
                    Result result = this.runStep(step, indexed, input, output, context);

                    if (result instanceof PipelineResult<?> pResult)
                        pResult.output().results().current().forEach(r -> output.results().register(indexed.uid(), r));
                    else
                        output.results().register(indexed.uid(), result);

                    StepStrategy strategy = step.postEvaluation(result);
                    if (strategy == StepStrategy.ABORT)
                    {
                        logger.trace("{}#{} received {} signal after step {} over argument {}", this.id(), output.tag().uid(), strategy, step.id(), indexed.uid());
                        break STEP_LOOP;
                    }
                    else if (strategy == StepStrategy.DISCARD_AND_CONTINUE)
                    {
                        logger.trace("{}#{} received {} signal after step {} over argument {}", this.id(), output.tag().uid(), strategy, step.id(), indexed.uid());
                        discarded.add(indexed);
                    }
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
        PipelineStepMetrics metrics = new PipelineStepMetrics(this.meterRegistry, this, step);

        long start = System.nanoTime();
        try {
            logger.trace("{}#{} running step {} over argument {}", this.id(), output.tag().uid(), step.id(), indexed.uid());
            Result result = step.execute(indexed, input, output.payload(), output.results().view(indexed), context);
            metrics.successCounter().increment();
            return result;
        }
        catch (Exception e) {
            logger.trace("{}#{} step {} threw an {}: {}", this.id(), output.tag().uid(), step.id(), e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            return step.handleException(e, context);
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
    private void runSinkSynchronously(SinkDescriptor<P> descriptor, Output<P> output, Context<P> context, PipelineSinkMetrics metrics) throws SinkException
    {
        long start = System.nanoTime();
        try {
            logger.trace("{}#{} launching sink {} (is-async: {})", this.id(), output.tag().uid(), descriptor.sink().getClass().getName(), false);
            descriptor.sink().execute(output, context);
            metrics.successCounter().increment();
        }
        catch (Exception e) {
            metrics.failureCounter().increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor<P> descriptor, Output<P> output, Context<P> context, PipelineSinkMetrics metrics)
    {
        logger.trace("{}#{} queuing sink {} (is-async: {})", this.id(), output.tag().uid(), descriptor.sink().getClass().getName(), true);
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            try {
                logger.trace("{}#{} launching sink {} (is-async: {})", this.id(), output.tag().uid(), descriptor.sink().getClass().getName(), true);
                descriptor.sink().execute(output, context);
                metrics.successCounter().increment();
            }
            catch (RuntimeException | SinkException e) {
                logger.error("{}#{} an error occurred while running an async sink: {}", this.id(), output.tag().uid(), e.getMessage());
                metrics.failureCounter().increment();
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
}
