package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.Tracer;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleAutoIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.input.initializer.builder.InitializerBuilder;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.manager.DefaultObservabilityManager;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.metering.tag.TagResolver;
import tech.illuin.pipeline.observer.Observer;
import tech.illuin.pipeline.output.factory.DefaultOutputFactory;
import tech.illuin.pipeline.output.factory.OutputFactory;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.builder.SinkAssembler;
import tech.illuin.pipeline.sink.builder.SinkBuilder;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;
import tech.illuin.pipeline.sink.runner.SinkRunner;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepBuilder;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PipelineStep;

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
    private OutputFactory<I> outputFactory;
    private UIDGenerator uidGenerator;
    private final List<StepAssembler<Indexable, I>> steps;
    private final List<SinkAssembler> sinks;
    private Supplier<ExecutorService> sinkExecutorProvider;
    private int closeTimeout;
    private final List<OnCloseHandler> onCloseHandlers;
    private ResultEvaluator defaultEvaluator;
    private PipelineErrorHandler errorHandler;
    private StepErrorHandler defaultStepErrorHandler;
    private SinkErrorHandler defaultSinkErrorHandler;
    private ObservabilityManager.Builder observabilityManagerBuilder;
    private TagResolver<I> tagResolver;
    private final List<Observer> observers;

    public SimplePipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.outputFactory = new DefaultOutputFactory<>();
        this.uidGenerator = KSUIDGenerator.INSTANCE;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.errorHandler = PipelineErrorHandler::wrapChecked;
        this.onCloseHandlers = new ArrayList<>();
        this.observabilityManagerBuilder = new DefaultObservabilityManager.Builder();
        this.observabilityManagerBuilder.setMeterRegistry(new SimpleMeterRegistry());
        this.observabilityManagerBuilder.setTracer(Tracer.NOOP);
        this.closeTimeout = 15;
        this.observers = new ArrayList<>();
    }

    public Pipeline<I> build()
    {
        return new CompositePipeline<>(
            this.id(),
            this.uidGenerator(),
            this.buildInitializer(),
            this.authorResolver(),
            Collections.singletonList(new SingleAutoIndexer<>()),
            this.outputFactory(),
            this.buildSteps(),
            this.buildSinks(),
            this.sinkExecutorProvider(),
            this.errorHandler(),
            this.closeTimeout(),
            this.onCloseHandlers(),
            this.observabilityManagerBuilder.build(),
            this.tagResolver() == null ? (in, ctx) -> new MetricTags() : this.tagResolver(),
            this.observers()
        );
    }

    private InitializerDescriptor<I> buildInitializer()
    {
        InitializerAssembler<I> initializerAssembler = builder -> builder.initializer(
            (in, ctx, gen) -> Initializer.initializeFromParentOr(ctx, () -> new VoidPayload(gen.generate()))
        );

        return initializerAssembler.build(new InitializerBuilder<>());
    }

    private List<StepDescriptor<Indexable, I>> buildSteps()
    {
        return this.steps.stream().map(assembler -> {
            StepBuilder<Indexable, I> stepBuilder = new StepBuilder<>();
            this.addAssemblerDefaults(stepBuilder);
            return assembler.build(stepBuilder);
        }).toList();
    }

    private List<SinkDescriptor> buildSinks()
    {
        return this.sinks.stream().map(assembler -> {
            SinkBuilder sinkBuilder = new SinkBuilder();
            this.addAssemblerDefaults(sinkBuilder);
            return assembler.build(sinkBuilder);
        }).toList();
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

    public SimplePipelineBuilder<I> registerSteps(List<? extends Step<?, I>> steps)
    {
        for (Step<?, I> step : steps)
            this.registerStep(step);
        return this;
    }

    public <ID extends Indexable> SimplePipelineBuilder<I> registerStep(Step<ID, I> step)
    {
        return this.registerStep((StepBuilder<ID, I> builder) -> {
            this.addAssemblerDefaults(builder);
            builder.step(step);
        });
    }

    public SimplePipelineBuilder<I> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<Indexable, I>) step);
    }

    public SimplePipelineBuilder<I> registerStep(Object target)
    {
        if (target instanceof PipelineStep<?> pipelineStep)
            //noinspection unchecked
            return this.registerStep((Step<?, I>) pipelineStep);
        return this.registerStep(Step.of(target));
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerStep(StepAssembler<?, I> builder)
    {
        this.steps.add((StepAssembler<Indexable, I>) builder);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> insertStep(StepAssembler<?, I> builder, int index)
    {
        this.steps.add(index, (StepAssembler<Indexable, I>) builder);
        return this;
    }

    public SimplePipelineBuilder<I> registerStepAssemblers(List<? extends StepAssembler<?, I>> builders)
    {
        for (StepAssembler<?, I> builder : builders)
            this.registerStep(builder);
        return this;
    }

    private void addAssemblerDefaults(StepBuilder<?, ?> stepBuilder)
    {
        stepBuilder
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultStepErrorHandler())
        ;
    }

    public SimplePipelineBuilder<I> registerSink(Sink sink)
    {
        return this.registerSink(builder -> builder.sink(sink));
    }

    public SimplePipelineBuilder<I> registerSink(Sink sink, boolean async)
    {
        return this.registerSink(builder -> builder
            .sink(sink)
            .setAsync(async)
        );
    }

    public SimplePipelineBuilder<I> registerSink(Object target)
    {
        return this.registerSink(new SinkRunner(target));
    }

    public SimplePipelineBuilder<I> registerSink(SinkAssembler assembler)
    {
        this.sinks.add(assembler);
        return this;
    }

    public SimplePipelineBuilder<I> registerSinks(List<? extends Sink> sinks)
    {
        for (Sink sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public SimplePipelineBuilder<I> registerSinks(List<? extends Sink> sinks, boolean async)
    {
        for (Sink sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public SimplePipelineBuilder<I> registerSinkAssemblers(List<? extends SinkAssembler> assemblers)
    {
        for (SinkAssembler assembler : assemblers)
            this.registerSink(assembler);
        return this;
    }

    private void addAssemblerDefaults(SinkBuilder sinkBuilder)
    {
        sinkBuilder.withErrorHandler(this.defaultSinkErrorHandler());
    }

    public Supplier<ExecutorService> sinkExecutorProvider()
    {
        return this.sinkExecutorProvider;
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

    public PipelineErrorHandler errorHandler()
    {
        return this.errorHandler;
    }

    public SimplePipelineBuilder<I> setErrorHandler(PipelineErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    public StepErrorHandler defaultStepErrorHandler()
    {
        return this.defaultStepErrorHandler;
    }

    public SinkErrorHandler defaultSinkErrorHandler()
    {
        return this.defaultSinkErrorHandler;
    }

    public SimplePipelineBuilder<I> setDefaultStepErrorHandler(StepErrorHandler defaultStepErrorHandler)
    {
        this.defaultStepErrorHandler = defaultStepErrorHandler;
        return this;
    }

    public SimplePipelineBuilder<I> setDefaultSinkErrorHandler(SinkErrorHandler defaultSinkErrorHandler)
    {
        this.defaultSinkErrorHandler = defaultSinkErrorHandler;
        return this;
    }

    /**
     * @deprecated Use {@link #observabilityManager()} instead
     */
    @Deprecated
    public MeterRegistry meterRegistry()
    {
        return this.observabilityManagerBuilder.meterRegistry();
    }

    /**
     * @deprecated Use {@link #addObservabilityComponent(MeterRegistry)} instead
     */
    @Deprecated
    public SimplePipelineBuilder<I> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.addObservabilityComponent(meterRegistry);
        return this;
    }

    public ObservabilityManager.Builder observabilityManager()
    {
        return this.observabilityManagerBuilder;
    }

    public SimplePipelineBuilder<I> setObservabilityManager(ObservabilityManager.Builder builder)
    {
        this.observabilityManagerBuilder = builder;
        return this;
    }

    public SimplePipelineBuilder<I> addObservabilityComponent(MeterRegistry meterRegistry)
    {
        this.observabilityManagerBuilder.setMeterRegistry(meterRegistry);
        return this;
    }

    public SimplePipelineBuilder<I> addObservabilityComponent(Tracer tracer)
    {
        this.observabilityManagerBuilder.setTracer(tracer);
        return this;
    }

    public TagResolver<I> tagResolver()
    {
        return tagResolver;
    }

    public SimplePipelineBuilder<I> setTagResolver(TagResolver<I> tagResolver)
    {
        this.tagResolver = tagResolver;
        return this;
    }

    public OutputFactory<I> outputFactory()
    {
        return this.outputFactory;
    }

    public SimplePipelineBuilder<I> setOutputFactory(OutputFactory<I> outputFactory)
    {
        this.outputFactory = outputFactory;
        return this;
    }

    public UIDGenerator uidGenerator()
    {
        return this.uidGenerator;
    }

    public SimplePipelineBuilder<I> setUidGenerator(UIDGenerator uidGenerator)
    {
        this.uidGenerator = uidGenerator;
        return this;
    }

    public List<Observer> observers()
    {
        return this.observers;
    }

    public SimplePipelineBuilder<I> registerObserver(Observer supplier)
    {
        this.observers.add(supplier);
        return this;
    }
}
