package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.indexer.MultiIndexer;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkDescriptor;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class PayloadPipelineBuilder<I, P> extends PipelineBuilder<I, P>
{
    private Initializer<I, P> initializer;
    private List<Indexer<P>> indexers;

    public PayloadPipelineBuilder()
    {
        this.initializer = (in, ctx) -> null;
        this.indexers = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Pipeline<I, P> build()
    {
        if (this.indexers().isEmpty())
            this.indexers.add((Indexer<P>) SingleIndexer.auto());

        return new CompositePipeline<>(
            this.id(),
            this.initializer(),
            this.authorResolver(),
            this.indexers(),
            this.steps(),
            this.sinks(),
            this.sinkExecutorProvider().get(),
            this.closeTimeout(),
            this.onCloseHandlers(),
            this.meterRegistry() == null ? new SimpleMeterRegistry() : this.meterRegistry()
        );
    }

    @Override
    public PayloadPipelineBuilder<I, P> setId(String id)
    {
        return (PayloadPipelineBuilder<I, P>) super.setId(id);
    }

    public Initializer<I, P> initializer()
    {
        return initializer;
    }

    public PayloadPipelineBuilder<I, P> setInitializer(Initializer<I, P> initializer)
    {
        this.initializer = initializer;
        return this;
    }

    public List<Indexer<P>> indexers()
    {
        return indexers;
    }

    public PayloadPipelineBuilder<I, P> setIndexers(List<Indexer<P>> indexers)
    {
        this.indexers = indexers;
        return this;
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

    @Override
    public PayloadPipelineBuilder<I, P> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        return (PayloadPipelineBuilder<I, P>) super.setAuthorResolver(authorResolver);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setSteps(List<StepDescriptor<Indexable, I, P>> steps)
    {
        return (PayloadPipelineBuilder<I, P>) super.setSteps(steps);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStep(IndexableStep<? extends T> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @SuppressWarnings("unchecked")
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStep(PayloadStep<P> step)
    {
        return this.registerStep((Step<T, I, P>) step);
    }

    @Override
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStep(Step<T, I, P> step)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerStep(step);
    }

    @Override
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStep(InputStep<I> step)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerStep(step);
    }

    @Override
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerSteps(List<Step<T, I, P>> steps)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerSteps(steps);
    }

    @Override
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStep(StepAssembler<T, I, P> builder)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerStep(builder);
    }

    @Override
    public <T extends Indexable> PayloadPipelineBuilder<I, P> registerStepAssemblers(List<StepAssembler<T, I, P>> builders)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerStepAssemblers(builders);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setSinks(List<SinkDescriptor<P>> sinks)
    {
        return (PayloadPipelineBuilder<I, P>) super.setSinks(sinks);
    }

    @Override
    public PayloadPipelineBuilder<I, P> registerSink(Sink<P> sink)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerSink(sink);
    }

    @Override
    public PayloadPipelineBuilder<I, P> registerSink(Collection<Sink<P>> sinks)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerSink(sinks);
    }

    @Override
    public PayloadPipelineBuilder<I, P> registerSink(Sink<P> sink, boolean async)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerSink(sink, async);
    }

    @Override
    public PayloadPipelineBuilder<I, P> registerSink(Collection<Sink<P>> sinks, boolean async)
    {
        return (PayloadPipelineBuilder<I, P>) super.registerSink(sinks, async);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        return (PayloadPipelineBuilder<I, P>) super.setSinkExecutorProvider(sinkExecutorProvider);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setSinkExecutor(ExecutorService sinkExecutor)
    {
        return (PayloadPipelineBuilder<I, P>) super.setSinkExecutor(sinkExecutor);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setCloseTimeout(int closeTimeout)
    {
        return (PayloadPipelineBuilder<I, P>) super.setCloseTimeout(closeTimeout);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        return (PayloadPipelineBuilder<I, P>) super.setDefaultEvaluator(defaultEvaluator);
    }

    @Override
    public PayloadPipelineBuilder<I, P> setDefaultErrorHandler(StepErrorHandler defaultErrorHandler)
    {
        return (PayloadPipelineBuilder<I, P>) super.setDefaultErrorHandler(defaultErrorHandler);
    }
}
