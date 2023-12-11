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
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.metering.tag.TagResolver;
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
    private OutputFactory<I, VoidPayload> outputFactory;
    private UIDGenerator uidGenerator;
    private final List<StepAssembler<Indexable, I, VoidPayload>> steps;
    private final List<SinkAssembler<VoidPayload>> sinks;
    private Supplier<ExecutorService> sinkExecutorProvider;
    private int closeTimeout;
    private final List<OnCloseHandler> onCloseHandlers;
    private ResultEvaluator defaultEvaluator;
    private StepErrorHandler defaultStepErrorHandler;
    private SinkErrorHandler defaultSinkErrorHandler;
    private MeterRegistry meterRegistry;
    private TagResolver<I> tagResolver;

    public SimplePipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.outputFactory = new DefaultOutputFactory<>();
        this.uidGenerator = KSUIDGenerator.INSTANCE;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.onCloseHandlers = new ArrayList<>();
        this.meterRegistry = null;
        this.closeTimeout = 15;
    }

    public Pipeline<I, VoidPayload> build()
    {
        return new CompositePipeline<>(
            this.id(),
            this.uidGenerator(),
            this.buildInitializer(),
            this.authorResolver(),
            Collections.singletonList(SingleIndexer.auto()),
            this.outputFactory(),
            this.buildSteps(),
            this.buildSinks(),
            this.sinkExecutorProvider().get(),
            this.closeTimeout(),
            this.onCloseHandlers(),
            this.meterRegistry() == null ? new SimpleMeterRegistry() : this.meterRegistry(),
            this.tagResolver() == null ? (in, ctx) -> new MetricTags() : this.tagResolver()
        );
    }

    private InitializerDescriptor<I, VoidPayload> buildInitializer()
    {
        InitializerAssembler<I, VoidPayload> initializerAssembler = builder -> builder.initializer(
            (in, ctx, gen) -> Initializer.initializeFromParentOr(ctx, () -> new VoidPayload(gen.generate()))
        );

        return initializerAssembler.build(new InitializerBuilder<>());
    }

    private List<StepDescriptor<Indexable, I, VoidPayload>> buildSteps()
    {
        return this.steps.stream().map(assembler -> {
            StepBuilder<Indexable, I, VoidPayload> stepBuilder = new StepBuilder<>();
            this.addAssemblerDefaults(stepBuilder);
            return assembler.build(stepBuilder);
        }).toList();
    }

    private List<SinkDescriptor<VoidPayload>> buildSinks()
    {
        return this.sinks.stream().map(assembler -> {
            SinkBuilder<VoidPayload> sinkBuilder = new SinkBuilder<>();
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

    public SimplePipelineBuilder<I> registerSteps(List<? extends Step<?, I, ?>> steps)
    {
        for (Step<?, I, ?> step : steps)
            this.registerStep(step);
        return this;
    }

    public <ID extends Indexable, T> SimplePipelineBuilder<I> registerStep(Step<ID, I, T> step)
    {
        return this.registerStep((StepBuilder<ID, I, T> builder) -> {
            this.addAssemblerDefaults(builder);
            builder.step(step);
        });
    }

    public SimplePipelineBuilder<I> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<Indexable, I, ?>) step);
    }

    public SimplePipelineBuilder<I> registerStep(Object target)
    {
        if (target instanceof PipelineStep<?, ?> pipelineStep)
            //noinspection unchecked
            return this.registerStep((Step<?, I, ?>) pipelineStep);
        return this.registerStep(Step.of(target));
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerStep(StepAssembler<?, I, ?> builder)
    {
        this.steps.add((StepAssembler<Indexable, I, VoidPayload>) builder);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> insertStep(StepAssembler<?, I, ?> builder, int index)
    {
        this.steps.add(index, (StepAssembler<Indexable, I, VoidPayload>) builder);
        return this;
    }

    public SimplePipelineBuilder<I> registerStepAssemblers(List<? extends StepAssembler<?, I, ?>> builders)
    {
        for (StepAssembler<?, I, ?> builder : builders)
            this.registerStep(builder);
        return this;
    }

    private void addAssemblerDefaults(StepBuilder<?, ?, ?> stepBuilder)
    {
        stepBuilder
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultStepErrorHandler())
        ;
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

    public SimplePipelineBuilder<I> registerSink(Object target)
    {
        return this.registerSink(new SinkRunner<>(target));
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> registerSink(SinkAssembler<?> assembler)
    {
        this.sinks.add((SinkAssembler<VoidPayload>) assembler);
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

    private void addAssemblerDefaults(SinkBuilder<?> sinkBuilder)
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

    public MeterRegistry meterRegistry()
    {
        return meterRegistry;
    }

    public SimplePipelineBuilder<I> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
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

    public OutputFactory<I, VoidPayload> outputFactory()
    {
        return this.outputFactory;
    }

    @SuppressWarnings("unchecked")
    public SimplePipelineBuilder<I> setOutputFactory(OutputFactory<I, ?> outputFactory)
    {
        this.outputFactory = (OutputFactory<I, VoidPayload>) outputFactory;
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
}
