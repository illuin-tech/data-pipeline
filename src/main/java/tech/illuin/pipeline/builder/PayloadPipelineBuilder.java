package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.input.initializer.builder.InitializerBuilder;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.factory.DefaultOutputFactory;
import tech.illuin.pipeline.output.factory.OutputFactory;
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
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class PayloadPipelineBuilder<I, P>
{
    private String id;
    private AuthorResolver<I> authorResolver;
    private OutputFactory<P> outputFactory;
    private UIDGenerator uidGenerator;
    private final List<StepDescriptor<Indexable, I, P>> steps;
    private final List<SinkDescriptor<P>> sinks;
    private Supplier<ExecutorService> sinkExecutorProvider;
    private int closeTimeout;
    private final List<OnCloseHandler> onCloseHandlers;
    private ResultEvaluator defaultEvaluator;
    private StepErrorHandler defaultErrorHandler;
    private MeterRegistry meterRegistry;
    private InitializerDescriptor<I, P> initializer;
    private final List<Indexer<P>> indexers;

    public PayloadPipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.outputFactory = new DefaultOutputFactory<>();
        this.uidGenerator = KSUIDGenerator.INSTANCE;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.onCloseHandlers = new ArrayList<>();
        this.meterRegistry = null;
        this.initializer = null;
        this.indexers = new ArrayList<>();
        this.closeTimeout = 15;
    }

    @SuppressWarnings("unchecked")
    public Pipeline<I, P> build()
    {
        if (this.initializer == null)
            throw new IllegalStateException("A payload-based pipeline cannot be built without an initializer");
        if (this.indexers().isEmpty())
            this.indexers.add((Indexer<P>) SingleIndexer.auto());

        return new CompositePipeline<>(
            this.id(),
            this.uidGenerator(),
            this.initializer(),
            this.authorResolver(),
            this.indexers(),
            this.outputFactory(),
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

    public PayloadPipelineBuilder<I, P> setId(String id)
    {
        this.id = id;
        return this;
    }

    public AuthorResolver<I> authorResolver()
    {
        return this.authorResolver;
    }

    public PayloadPipelineBuilder<I, P> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        this.authorResolver = authorResolver;
        return this;
    }

    public List<StepDescriptor<Indexable, I, P>> steps()
    {
        return this.steps;
    }

    public PayloadPipelineBuilder<I, P> registerSteps(List<? extends Step<? extends Indexable, I, ? extends P>> steps)
    {
        for (Step<? extends Indexable, I, ? extends P> step : steps)
            this.registerStep(step);
        return this;
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerStep(Step<? extends Indexable, I, ? extends P> step)
    {
        return this.registerStep(builder -> builder
            .step((Step<Indexable, I, P>) step)
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultErrorHandler())
        );
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<Indexable, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerStep(PayloadStep<? extends P> step)
    {
        return this.registerStep((Step<Indexable, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerStep(IndexableStep<? extends Indexable> step)
    {
        return this.registerStep((Step<Indexable, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerStep(StepAssembler<? extends Indexable, I, ? extends P> assembler)
    {
        this.steps.add(((StepAssembler<Indexable, I, P>) assembler).build(new StepBuilder<>()));
        return this;
    }

    public PayloadPipelineBuilder<I, P> registerStepAssemblers(List<? extends StepAssembler<? extends Indexable, I, ? extends P>> assemblers)
    {
        for (StepAssembler<? extends Indexable, I, ? extends P> assembler : assemblers)
            this.registerStep(assembler);
        return this;
    }

    public List<SinkDescriptor<P>> sinks()
    {
        return this.sinks;
    }

    public PayloadPipelineBuilder<I, P> registerSink(Sink<? extends P> sink)
    {
        return this.registerSink(sink, false);
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerSink(Sink<? extends P> sink, boolean async)
    {
        return this.registerSink(builder -> builder
            .sink((Sink<P>) sink)
            .setAsync(async)
        );
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> registerSink(SinkAssembler<? extends P> assembler)
    {
        this.sinks.add(((SinkAssembler<P>) assembler).build(new SinkBuilder<>()));
        return this;
    }

    public PayloadPipelineBuilder<I, P> registerSinks(List<? extends Sink<? extends P>> sinks)
    {
        for (Sink<? extends P> sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public PayloadPipelineBuilder<I, P> registerSinks(List<? extends Sink<? extends P>> sinks, boolean async)
    {
        for (Sink<? extends P> sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public PayloadPipelineBuilder<I, P> registerSinkAssemblers(List<? extends SinkAssembler<? extends P>> assemblers)
    {
        for (SinkAssembler<? extends P> assembler : assemblers)
            this.registerSink(assembler);
        return this;
    }

    public Supplier<ExecutorService> sinkExecutorProvider()
    {
        return this.sinkExecutorProvider;
    }

    public PayloadPipelineBuilder<I, P> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        this.sinkExecutorProvider = sinkExecutorProvider;
        return this;
    }

    public PayloadPipelineBuilder<I, P> setSinkExecutor(ExecutorService sinkExecutor)
    {
        this.sinkExecutorProvider = () -> sinkExecutor;
        return this;
    }

    public List<OnCloseHandler> onCloseHandlers()
    {
        return this.onCloseHandlers;
    }

    public PayloadPipelineBuilder<I, P> registerOnCloseHandler(OnCloseHandler handler)
    {
        this.onCloseHandlers.add(handler);
        return this;
    }

    public int closeTimeout()
    {
        return this.closeTimeout;
    }

    public PayloadPipelineBuilder<I, P> setCloseTimeout(int closeTimeout)
    {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public ResultEvaluator defaultEvaluator()
    {
        return this.defaultEvaluator;
    }

    public PayloadPipelineBuilder<I, P> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        this.defaultEvaluator = defaultEvaluator;
        return this;
    }

    public StepErrorHandler defaultErrorHandler()
    {
        return this.defaultErrorHandler;
    }

    public PayloadPipelineBuilder<I, P> setDefaultErrorHandler(StepErrorHandler defaultErrorHandler)
    {
        this.defaultErrorHandler = defaultErrorHandler;
        return this;
    }

    public MeterRegistry meterRegistry()
    {
        return meterRegistry;
    }

    public PayloadPipelineBuilder<I, P> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
        return this;
    }

    public InitializerDescriptor<I, P> initializer()
    {
        return this.initializer;
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> setInitializer(Initializer<I, ? extends P> initializer)
    {
        return this.setInitializer(builder -> builder.initializer((Initializer<I, P>) initializer));
    }

    @SuppressWarnings("unchecked")
    public PayloadPipelineBuilder<I, P> setInitializer(InitializerAssembler<I, ? extends P> assembler)
    {
        this.initializer = (InitializerDescriptor<I, P>) assembler.build(new InitializerBuilder<>());
        return this;
    }

    public List<Indexer<P>> indexers()
    {
        return this.indexers;
    }

    public PayloadPipelineBuilder<I, P> registerIndexer(SingleIndexer<P> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    public PayloadPipelineBuilder<I, P> registerIndexer(MultiIndexer<P> indexer)
    {
        this.indexers.add(indexer);
        return this;
    }

    public OutputFactory<P> outputFactory()
    {
        return this.outputFactory;
    }

    public PayloadPipelineBuilder<I, P> setOutputFactory(OutputFactory<P> outputFactory)
    {
        this.outputFactory = outputFactory;
        return this;
    }

    public UIDGenerator uidGenerator()
    {
        return this.uidGenerator;
    }

    public PayloadPipelineBuilder<I, P> setUidGenerator(UIDGenerator uidGenerator)
    {
        this.uidGenerator = uidGenerator;
        return this;
    }
}
