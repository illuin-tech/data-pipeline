package tech.illuin.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkDescriptor;
import tech.illuin.pipeline.sink.SinkException;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepBuilder;
import tech.illuin.pipeline.step.StepStrategy;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class CompositePipeline<I, P> implements Pipeline<I, P>, AutoCloseable
{
    private final String id;
    private Initializer<I, P> initializer;
    private AuthorResolver<I> authorResolver;
    private final List<Indexer<P>> indexers;
    private final List<Step<Indexable, I, P>> steps;
    private final List<SinkDescriptor<P>> sinks;

    private final ExecutorService sinkExecutor;
    private int closeTimeout;

    private static final Logger logger = LoggerFactory.getLogger(CompositePipeline.class);

    public CompositePipeline(String id)
    {
        this(id, Runtime.getRuntime().availableProcessors());
    }

    public CompositePipeline(String id, int sinkParallelism)
    {
        this(id, Executors.newFixedThreadPool(sinkParallelism));
    }

    public CompositePipeline(String id, ExecutorService sinkExecutor)
    {
        this.id = id;
        this.initializer = (in, ctx) -> null;
        this.authorResolver = AuthorResolver::anonymous;
        this.indexers = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        /* Beware of using a ForkJoinPool, it does not play nice with the Spring ClassLoader ; if absolutely needed a custom ForkJoinWorkerThreadFactory would be required. */
        this.sinkExecutor = sinkExecutor;
        this.closeTimeout = 5;
    }

    @Override
    public String id()
    {
        return this.id;
    }

    @Override
    public Output<P> run(I input, Context<P> context) throws PipelineException
    {
        try {
            logger.debug("{}: launching pipeline over input of type {}", this.id(), input != null ? input.getClass().getName() : "null");

            Output<P> output = this.runInitialization(input, context);
            this.runSteps(input, output, context);
            this.runSinks(output, context);

            logger.trace("{}#{} finished", this.id(), output.tag().uid());

            return output;
        }
        catch (SinkException e) {
            throw new PipelineException(e);
        }
    }

    private Output<P> runInitialization(I input, Context<P> context)
    {
        Output<P> output = new Output<>(
            this.id(),
            this.authorResolver.resolve(input, context)
        );
        UUID runUid = output.tag().uid();

        logger.trace("{}#{} initializing payload", this.id(), runUid);
        output.setPayload(this.initializer.initialize(input, context));

        for (Indexer<P> indexer : this.indexers)
        {
            logger.trace("{}#{} launching indexer {}", this.id(), runUid, indexer.getClass().getName());
            indexer.index(output.payload(), output.index());
        }

        return output;
    }

    private void runSteps(I input, Output<P> output, Context<P> context)
    {
        UUID runUid = output.tag().uid();
        Set<Indexable> discarded = new HashSet<>();
        STEP_LOOP: for (Step<Indexable, I, P> step : this.steps)
        {
            /* Arguments are a list of Indexable which satisfy the step's execution predicate */
            List<Indexable> arguments = output.index().stream()
                .filter(idx -> !discarded.contains(idx))
                .filter(step::canExecute)
                .toList()
            ;

            logger.trace("{}#{} retrieved {} arguments for step {}", this.id(), runUid, arguments.size(), step.id());

            /* For each argument we perform the step and register the produced Result */
            for (Indexable indexed : arguments)
            {
                logger.trace("{}#{} running step {} over argument {}", this.id(), runUid, step.id(), indexed.uid());
                Result result = step.execute(indexed, input, output.payload(), context);
                output.results().register(indexed.uid(), result);

                StepStrategy strategy = step.postEvaluation(result);
                if (strategy == StepStrategy.ABORT)
                {
                    logger.trace("{}#{} received {} signal after step {} over argument {}", this.id(), runUid, strategy, step.id(), indexed.uid());
                    break STEP_LOOP;
                }
                else if (strategy == StepStrategy.DISCARD_AND_CONTINUE)
                {
                    logger.trace("{}#{} received {} signal after step {} over argument {}", this.id(), runUid, strategy, step.id(), indexed.uid());
                    discarded.add(indexed);
                }
            }
        }
    }

    private void runSinks(Output<P> output, Context<P> context) throws SinkException
    {
        UUID runUid = output.tag().uid();
        for (SinkDescriptor<P> descriptor : this.sinks)
        {
            if (descriptor.isAsync())
            {
                logger.trace("{}#{} queuing sink {} (is-async: {})", this.id(), runUid, descriptor.sink().getClass().getName(), true);
                CompletableFuture.runAsync(() -> {
                    try {
                        logger.trace("{}#{} launching sink {} (is-async: {})", this.id(), runUid, descriptor.sink().getClass().getName(), true);
                        descriptor.sink().execute(output, context);
                    }
                    catch (RuntimeException | SinkException e) {
                        logger.error("{}#{} an error occurred while running an async sink: {}", this.id(), runUid, e.getMessage());
                    }
                }, this.sinkExecutor);
            }
            else {
                logger.trace("{}#{} launching sink {} (is-async: {})", this.id(), runUid, descriptor.sink().getClass().getName(), false);
                descriptor.sink().execute(output, context);
            }
        }
    }

    public CompositePipeline<I, P> registerAuthorResolver(AuthorResolver<I> authorResolver)
    {
        this.authorResolver = authorResolver;
        return this;
    }

    public CompositePipeline<I, P> registerInitializer(Initializer<I, P> initializer)
    {
        this.initializer = initializer;
        return this;
    }

    public CompositePipeline<I, P> registerIndexer(SingleIndexer<P> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    public CompositePipeline<I, P> registerIndexer(MultiIndexer<P> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> CompositePipeline<I, P> registerStep(Step<T, I, P> step)
    {
        this.steps.add((Step<Indexable, I, P>) step);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> CompositePipeline<I, P> registerStep(IndexableStep<? extends T> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> CompositePipeline<I, P> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> CompositePipeline<I, P> registerStep(PayloadStep<P> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> CompositePipeline<I, P> registerStep(StepBuilder<T, I, P> builder)
    {
        return this.registerStep((Step<Indexable, I, P>) builder.build(new Step.Builder<>()));
    }

    public CompositePipeline<I, P> registerSink(Sink<P> sink)
    {
        return this.registerSink(sink, false);
    }

    public CompositePipeline<I, P> registerSink(Collection<Sink<P>> sinks)
    {
        for (Sink<P> sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public CompositePipeline<I, P> registerSink(Sink<P> sink, boolean async)
    {
        this.sinks.add(new SinkDescriptor<>(sink, async));
        return this;
    }

    public CompositePipeline<I, P> registerSink(Collection<Sink<P>> sinks, boolean async)
    {
        for (Sink<P> sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public CompositePipeline<I, P> setCloseTimeout(int timeout)
    {
        this.closeTimeout = timeout;
        return this;
    }

    @Override
    synchronized public void close() throws Exception
    {
        if (this.sinkExecutor.isShutdown())
            return;

        this.sinkExecutor.shutdown();
        boolean success = this.sinkExecutor.awaitTermination(this.closeTimeout, TimeUnit.SECONDS);
        var status = success ? "done" : "timeout after " + this.closeTimeout + " seconds";
        logger.info("{} closed (executor termination status: {})", this.id(), status);
    }
}
