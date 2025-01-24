package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.execution.error.PipelineErrorHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.*;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.input.initializer.builder.InitializerBuilder;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.initializer.runner.InitializerRunner;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
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
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;
import tech.illuin.pipeline.step.variant.PipelineStep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class PayloadPipelineBuilder<I>
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
    private MeterRegistry meterRegistry;
    private TagResolver<I> tagResolver;
    private InitializerAssembler<I> initializer;
    private final List<Indexer<?>> indexers;
    private final List<Observer> observers;

    public PayloadPipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.outputFactory = new DefaultOutputFactory<>();
        this.uidGenerator = KSUIDGenerator.INSTANCE;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.errorHandler = PipelineErrorHandler::wrapChecked;
        this.onCloseHandlers = new ArrayList<>();
        this.meterRegistry = null;
        this.initializer = null;
        this.indexers = new ArrayList<>();
        this.closeTimeout = 15;
        this.observers = new ArrayList<>();
    }

    public Pipeline<I> build()
    {
        if (this.initializer == null)
            throw new IllegalStateException("A payload-based pipeline cannot be built without an initializer");
        if (this.indexers().isEmpty())
            this.indexers.add(new SingleAutoIndexer<>());
        if (this.id == null)
            throw new IllegalStateException("The pipeline id cannot be null");

        return new CompositePipeline<>(
            this.id(),
            this.uidGenerator(),
            this.buildInitializer(),
            this.authorResolver(),
            this.indexers(),
            this.outputFactory(),
            this.buildSteps(),
            this.buildSinks(),
            this.sinkExecutorProvider(),
            this.errorHandler(),
            this.closeTimeout(),
            this.onCloseHandlers(),
            this.meterRegistry() == null ? new SimpleMeterRegistry() : this.meterRegistry(),
            this.tagResolver() == null ? (in, ctx) -> new MetricTags() : this.tagResolver(),
            this.observers()
        );
    }

    private InitializerDescriptor<I> buildInitializer()
    {
        return this.initializer.build(new InitializerBuilder<>());
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

    public PayloadPipelineBuilder<I> setId(String id)
    {
        this.id = id;
        return this;
    }

    public AuthorResolver<I> authorResolver()
    {
        return this.authorResolver;
    }

    public PayloadPipelineBuilder<I> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        this.authorResolver = authorResolver;
        return this;
    }

    public PayloadPipelineBuilder<I> registerSteps(List<? extends Step<? extends Indexable, I>> steps)
    {
        for (Step<? extends Indexable, I> step : steps)
            this.registerStep(step);
        return this;
    }

    public PayloadPipelineBuilder<I> registerStep(Step<? extends Indexable, I> step)
    {
        return this.registerStep(builder -> {
            this.addAssemblerDefaults(builder);
            builder.step(step);
        });
    }

    public PayloadPipelineBuilder<I> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<Indexable, I>) step);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I> registerStep(PayloadStep step)
    {
        return this.registerStep((Step<Indexable, I>) step);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I> registerStep(IndexableStep<? extends Indexable> step)
    {
        return this.registerStep((Step<Indexable, I>) step);
    }

    public PayloadPipelineBuilder<I> registerStep(Object target)
    {
        if (target instanceof PipelineStep<?> pipelineStep)
            return this.registerStep(pipelineStep);
        return this.registerStep(Step.of(target));
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I> registerStep(StepAssembler<? extends Indexable, I> assembler)
    {
        this.steps.add((StepAssembler<Indexable, I>) assembler);
        return this;
    }

    public PayloadPipelineBuilder<I> registerStepAssemblers(List<? extends StepAssembler<? extends Indexable, I>> assemblers)
    {
        for (StepAssembler<? extends Indexable, I> assembler : assemblers)
            this.registerStep(assembler);
        return this;
    }

    private void addAssemblerDefaults(StepBuilder<?, ?> stepBuilder)
    {
        stepBuilder
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultStepErrorHandler())
        ;
    }

    public PayloadPipelineBuilder<I> registerSink(Sink sink)
    {
        return this.registerSink(sink, false);
    }

    public PayloadPipelineBuilder<I> registerSink(Object target)
    {
        return this.registerSink(new SinkRunner(target));
    }

    public PayloadPipelineBuilder<I> registerSink(Sink sink, boolean async)
    {
        return this.registerSink(builder -> builder
            .sink(sink)
            .setAsync(async)
        );
    }

    public PayloadPipelineBuilder<I> registerSink(SinkAssembler assembler)
    {
        this.sinks.add(assembler);
        return this;
    }

    public PayloadPipelineBuilder<I> registerSinks(List<? extends Sink> sinks)
    {
        for (Sink sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public PayloadPipelineBuilder<I> registerSinks(List<? extends Sink> sinks, boolean async)
    {
        for (Sink sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public PayloadPipelineBuilder<I> registerSinkAssemblers(List<? extends SinkAssembler> assemblers)
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

    public PayloadPipelineBuilder<I> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        this.sinkExecutorProvider = sinkExecutorProvider;
        return this;
    }

    public PayloadPipelineBuilder<I> setSinkExecutor(ExecutorService sinkExecutor)
    {
        this.sinkExecutorProvider = () -> sinkExecutor;
        return this;
    }

    public List<OnCloseHandler> onCloseHandlers()
    {
        return this.onCloseHandlers;
    }

    public PayloadPipelineBuilder<I> registerOnCloseHandler(OnCloseHandler handler)
    {
        this.onCloseHandlers.add(handler);
        return this;
    }

    public int closeTimeout()
    {
        return this.closeTimeout;
    }

    public PayloadPipelineBuilder<I> setCloseTimeout(int closeTimeout)
    {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public ResultEvaluator defaultEvaluator()
    {
        return this.defaultEvaluator;
    }

    public PayloadPipelineBuilder<I> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        this.defaultEvaluator = defaultEvaluator;
        return this;
    }

    public PipelineErrorHandler errorHandler()
    {
        return this.errorHandler;
    }

    public PayloadPipelineBuilder<I> setErrorHandler(PipelineErrorHandler errorHandler)
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

    public PayloadPipelineBuilder<I> setDefaultStepErrorHandler(StepErrorHandler defaultStepErrorHandler)
    {
        this.defaultStepErrorHandler = defaultStepErrorHandler;
        return this;
    }

    public PayloadPipelineBuilder<I> setDefaultSinkErrorHandler(SinkErrorHandler defaultSinkErrorHandler)
    {
        this.defaultSinkErrorHandler = defaultSinkErrorHandler;
        return this;
    }

    public MeterRegistry meterRegistry()
    {
        return meterRegistry;
    }

    public PayloadPipelineBuilder<I> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
        return this;
    }

    public TagResolver<I> tagResolver()
    {
        return tagResolver;
    }

    public PayloadPipelineBuilder<I> setTagResolver(TagResolver<I> tagResolver)
    {
        this.tagResolver = tagResolver;
        return this;
    }

    public PayloadPipelineBuilder<I> setInitializer(Initializer<I> initializer)
    {
        return this.setInitializer(builder -> builder.initializer(initializer));
    }

    public PayloadPipelineBuilder<I> setInitializer(Object target)
    {
        return this.setInitializer(new InitializerRunner<>(target));
    }

    public PayloadPipelineBuilder<I> setInitializer(InitializerAssembler<I> assembler)
    {
        this.initializer = assembler;
        return this;
    }

    public List<Indexer<?>> indexers()
    {
        return this.indexers;
    }

    public PayloadPipelineBuilder<I> registerIndexer(SingleIndexer<?> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    public PayloadPipelineBuilder<I> registerIndexer(MultiIndexer<?> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    public OutputFactory<I> outputFactory()
    {
        return this.outputFactory;
    }

    public PayloadPipelineBuilder<I> setOutputFactory(OutputFactory<I> outputFactory)
    {
        this.outputFactory = outputFactory;
        return this;
    }

    public UIDGenerator uidGenerator()
    {
        return this.uidGenerator;
    }

    public PayloadPipelineBuilder<I> setUidGenerator(UIDGenerator uidGenerator)
    {
        this.uidGenerator = uidGenerator;
        return this;
    }

    public List<Observer> observers()
    {
        return this.observers;
    }

    public PayloadPipelineBuilder<I> registerObserver(Observer supplier)
    {
        this.observers.add(supplier);
        return this;
    }
}
