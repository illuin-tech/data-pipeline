package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.input.initializer.builder.InitializerBuilder;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.builder.SinkAssembler;
import tech.illuin.pipeline.sink.builder.SinkBuilder;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepBuilder;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class SimplePipelineBuilder<I>
{
    private String id;
    private AuthorResolver<I> authorResolver;
    private final List<StepDescriptor<Indexable, I, VoidPayload>> steps;
    private final List<SinkDescriptor<VoidPayload>> sinks;
    private Supplier<ExecutorService> sinkExecutorProvider;
    private int closeTimeout;
    private final List<OnCloseHandler> onCloseHandlers;
    private ResultEvaluator defaultEvaluator;
    private StepErrorHandler defaultErrorHandler;
    private MeterRegistry meterRegistry;

    public SimplePipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.onCloseHandlers = new ArrayList<>();
        this.meterRegistry = null;
        this.closeTimeout = 15;
    }

    public Pipeline<I, VoidPayload> build()
    {
        InitializerAssembler<I, VoidPayload> initializerAssembler = builder -> builder.initializer(
            (in, ctx) -> Initializer.initializeFromParentOr(ctx, VoidPayload::new)
        );

        return new CompositePipeline<>(
            this.id(),
            initializerAssembler.build(new InitializerBuilder<>()),
            this.authorResolver(),
            Collections.singletonList(SingleIndexer.auto()),
            this.steps(),
            this.sinks(),
            this.sinkExecutorProvider().get(),
            this.closeTimeout(),
            this.onCloseHandlers(),
            this.meterRegistry() == null ? new SimpleMeterRegistry() : this.meterRegistry()
        );
    }

    public String id()
    {
        return this.id;
    }

    public SimplePipelineBuilder<I> setId(String id)
    {
        this.id = id;
        return this;
    }

    public AuthorResolver<I> authorResolver()
    {
        return this.authorResolver;
    }

    public SimplePipelineBuilder<I> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        this.authorResolver = authorResolver;
        return this;
    }

    public List<StepDescriptor<Indexable, I, VoidPayload>> steps()
    {
        return this.steps;
    }

    public SimplePipelineBuilder<I> registerSteps(List<? extends Step<?, I, ?>> steps)
    {
        for (Step<?, I, ?> step : steps)
            this.registerStep(step);
        return this;
    }

    public <ID extends Indexable, T> SimplePipelineBuilder<I> registerStep(Step<ID, I, T> step)
    {
        return this.registerStep((StepBuilder<ID, I, T> builder) -> builder
            .step(step)
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultErrorHandler())
        );
    }

    public SimplePipelineBuilder<I> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<Indexable, I, ?>) step);
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerStep(StepAssembler<?, I, ?> builder)
    {
        this.steps.add(((StepAssembler<Indexable, I, VoidPayload>) builder).build(new StepBuilder<>()));
        return this;
    }

    public SimplePipelineBuilder<I> registerStepAssemblers(List<? extends StepAssembler<?, I, ?>> builders)
    {
        for (StepAssembler<?, I, ?> builder : builders)
            this.registerStep(builder);
        return this;
    }

    public List<SinkDescriptor<VoidPayload>> sinks()
    {
        return this.sinks;
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerSink(Sink<?> sink)
    {
        return this.registerSink(builder -> builder.sink((Sink<Object>) sink));
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerSink(Sink<?> sink, boolean async)
    {
        return this.registerSink(builder -> builder
            .sink((Sink<Object>) sink)
            .setAsync(async)
        );
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerSink(SinkAssembler<?> assembler)
    {
        this.sinks.add(((SinkAssembler<VoidPayload>) assembler).build(new SinkBuilder<>()));
        return this;
    }

    public SimplePipelineBuilder<I> registerSinks(List<? extends Sink<?>> sinks)
    {
        for (Sink<?> sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public SimplePipelineBuilder<I> registerSinks(List<? extends Sink<?>> sinks, boolean async)
    {
        for (Sink<?> sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public SimplePipelineBuilder<I> registerSinkAssemblers(List<? extends SinkAssembler<?>> assemblers)
    {
        for (SinkAssembler<?> assembler : assemblers)
            this.registerSink(assembler);
        return this;
    }

    public Supplier<ExecutorService> sinkExecutorProvider()
    {
        return sinkExecutorProvider;
    }

    public SimplePipelineBuilder<I> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        this.sinkExecutorProvider = sinkExecutorProvider;
        return this;
    }

    public SimplePipelineBuilder<I> setSinkExecutor(ExecutorService sinkExecutor)
    {
        this.sinkExecutorProvider = () -> sinkExecutor;
        return this;
    }

    public List<OnCloseHandler> onCloseHandlers()
    {
        return this.onCloseHandlers;
    }

    public SimplePipelineBuilder<I> registerOnCloseHandler(OnCloseHandler handler)
    {
        this.onCloseHandlers.add(handler);
        return this;
    }

    public int closeTimeout()
    {
        return this.closeTimeout;
    }

    public SimplePipelineBuilder<I> setCloseTimeout(int closeTimeout)
    {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public ResultEvaluator defaultEvaluator()
    {
        return this.defaultEvaluator;
    }

    public SimplePipelineBuilder<I> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        this.defaultEvaluator = defaultEvaluator;
        return this;
    }

    public StepErrorHandler defaultErrorHandler()
    {
        return this.defaultErrorHandler;
    }

    public SimplePipelineBuilder<I> setDefaultErrorHandler(StepErrorHandler defaultErrorHandler)
    {
        this.defaultErrorHandler = defaultErrorHandler;
        return this;
    }

    public MeterRegistry meterRegistry()
    {
        return meterRegistry;
    }

    public SimplePipelineBuilder<I> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
        return this;
    }
}
