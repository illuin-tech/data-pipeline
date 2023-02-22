package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.MeterRegistry;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.close.OnCloseHandler;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkDescriptor;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepBuilder;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public abstract class PipelineBuilder<I, P>
{
    private String id;
    private AuthorResolver<I> authorResolver;
    private List<StepDescriptor<Indexable, I, P>> steps;
    private List<SinkDescriptor<P>> sinks;
    private Supplier<ExecutorService> sinkExecutorProvider;
    private int closeTimeout;
    private List<OnCloseHandler> onCloseHandlers;
    private ResultEvaluator defaultEvaluator;
    private StepErrorHandler defaultErrorHandler;
    private MeterRegistry meterRegistry;

    public PipelineBuilder()
    {
        this.authorResolver = AuthorResolver::anonymous;
        this.sinkExecutorProvider = () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.steps = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.onCloseHandlers = new ArrayList<>();
        this.meterRegistry = null;
    }

    public abstract Pipeline<I, P> build();

    public String id()
    {
        return id;
    }

    public PipelineBuilder<I, P> setId(String id)
    {
        this.id = id;
        return this;
    }

    public AuthorResolver<I> authorResolver()
    {
        return authorResolver;
    }

    public PipelineBuilder<I, P> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        this.authorResolver = authorResolver;
        return this;
    }

    public List<StepDescriptor<Indexable, I, P>> steps()
    {
        return steps;
    }

    public PipelineBuilder<I, P> setSteps(List<StepDescriptor<Indexable, I, P>> steps)
    {
        this.steps = steps;
        return this;
    }

    public <T extends Indexable> PipelineBuilder<I, P> registerStep(Step<T, I, P> step)
    {
        return this.registerStep(builder -> builder
            .step(step)
            .withEvaluation(this.defaultEvaluator())
            .withErrorHandler(this.defaultErrorHandler())
        );
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> PipelineBuilder<I, P> registerStep(InputStep<I> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> PipelineBuilder<I, P> registerStep(StepAssembler<T, I, P> builder)
    {
        this.steps.add((StepDescriptor<Indexable, I, P>) builder.build(new StepBuilder<>()));
        return this;
    }

    public List<SinkDescriptor<P>> sinks()
    {
        return sinks;
    }

    public PipelineBuilder<I, P> setSinks(List<SinkDescriptor<P>> sinks)
    {
        this.sinks = sinks;
        return this;
    }

    public PipelineBuilder<I, P> registerSink(Sink<P> sink)
    {
        return this.registerSink(sink, false);
    }

    public PipelineBuilder<I, P> registerSink(Collection<Sink<P>> sinks)
    {
        for (Sink<P> sink : sinks)
            this.registerSink(sink, false);
        return this;
    }

    public PipelineBuilder<I, P> registerSink(Sink<P> sink, boolean async)
    {
        this.sinks.add(new SinkDescriptor<>(sink, async));
        return this;
    }

    public PipelineBuilder<I, P> registerSink(Collection<Sink<P>> sinks, boolean async)
    {
        for (Sink<P> sink : sinks)
            this.registerSink(sink, async);
        return this;
    }

    public Supplier<ExecutorService> sinkExecutorProvider()
    {
        return sinkExecutorProvider;
    }

    public PipelineBuilder<I, P> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        this.sinkExecutorProvider = sinkExecutorProvider;
        return this;
    }

    public PipelineBuilder<I, P> setSinkExecutor(ExecutorService sinkExecutor)
    {
        this.sinkExecutorProvider = () -> sinkExecutor;
        return this;
    }

    public int closeTimeout()
    {
        return closeTimeout;
    }

    public PipelineBuilder<I, P> setCloseTimeout(int closeTimeout)
    {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public List<OnCloseHandler> onCloseHandlers()
    {
        return onCloseHandlers;
    }

    public PipelineBuilder<I, P> setOnCloseHandlers(List<OnCloseHandler> onCloseHandlers)
    {
        this.onCloseHandlers = onCloseHandlers;
        return this;
    }

    public PipelineBuilder<I, P> registerOnCloseHandler(OnCloseHandler handler)
    {
        this.onCloseHandlers.add(handler);
        return this;
    }

    public ResultEvaluator defaultEvaluator()
    {
        return defaultEvaluator;
    }

    public PipelineBuilder<I, P> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        this.defaultEvaluator = defaultEvaluator;
        return this;
    }

    public StepErrorHandler defaultErrorHandler()
    {
        return defaultErrorHandler;
    }

    public PipelineBuilder<I, P> setDefaultErrorHandler(StepErrorHandler defaultErrorHandler)
    {
        this.defaultErrorHandler = defaultErrorHandler;
        return this;
    }

    public MeterRegistry meterRegistry()
    {
        return meterRegistry;
    }

    public PipelineBuilder<I, P> setMeterRegistry(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
        return this;
    }
}
