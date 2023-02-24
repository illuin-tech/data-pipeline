package tech.illuin.pipeline.builder;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.illuin.pipeline.CompositePipeline;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.SinkDescriptor;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.builder.StepAssembler;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.variant.InputStep;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class SimplePipelineBuilder<I> extends PipelineBuilder<I, VoidPayload>
{
    @Override
    public Pipeline<I, VoidPayload> build()
    {
        return new CompositePipeline<>(
            this.id(),
            (in, ctx) -> Initializer.initializeFromParentOr(in, ctx, VoidPayload::new),
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

    @Override
    public SimplePipelineBuilder<I> setId(String id)
    {
        return (SimplePipelineBuilder<I>) super.setId(id);
    }

    @Override
    public SimplePipelineBuilder<I> setAuthorResolver(AuthorResolver<I> authorResolver)
    {
        return (SimplePipelineBuilder<I>) super.setAuthorResolver(authorResolver);
    }

    @Override
    public SimplePipelineBuilder<I> setSteps(List<StepDescriptor<Indexable, I, VoidPayload>> steps)
    {
        return (SimplePipelineBuilder<I>) super.setSteps(steps);
    }

    @Override
    public <T extends Indexable> SimplePipelineBuilder<I> registerStep(Step<T, I, VoidPayload> step)
    {
        return (SimplePipelineBuilder<I>) super.registerStep(step);
    }

    @Override
    public <T extends Indexable> SimplePipelineBuilder<I> registerStep(InputStep<I> step)
    {
        return (SimplePipelineBuilder<I>) super.registerStep(step);
    }

    @Override
    public <T extends Indexable> SimplePipelineBuilder<I> registerSteps(List<? extends Step<T, I, VoidPayload>> steps)
    {
        return (SimplePipelineBuilder<I>) super.registerSteps(steps);
    }

    @Override
    public <T extends Indexable> SimplePipelineBuilder<I> registerStep(StepAssembler<T, I, VoidPayload> builder)
    {
        return (SimplePipelineBuilder<I>) super.registerStep(builder);
    }

    @Override
    public <T extends Indexable> SimplePipelineBuilder<I> registerStepAssemblers(List<? extends StepAssembler<T, I, VoidPayload>> builders)
    {
        return (SimplePipelineBuilder<I>) super.registerStepAssemblers(builders);
    }

    @Override
    public SimplePipelineBuilder<I> setSinks(List<SinkDescriptor<VoidPayload>> sinks)
    {
        return (SimplePipelineBuilder<I>) super.setSinks(sinks);
    }

    @Override
    public SimplePipelineBuilder<I> registerSink(Sink<VoidPayload> sink)
    {
        return (SimplePipelineBuilder<I>) super.registerSink(sink);
    }

    @Override
    public SimplePipelineBuilder<I> registerSink(Collection<Sink<VoidPayload>> sinks)
    {
        return (SimplePipelineBuilder<I>) super.registerSink(sinks);
    }

    @Override
    public SimplePipelineBuilder<I> registerSink(Sink<VoidPayload> sink, boolean async)
    {
        return (SimplePipelineBuilder<I>) super.registerSink(sink, async);
    }

    @Override
    public SimplePipelineBuilder<I> registerSink(Collection<Sink<VoidPayload>> sinks, boolean async)
    {
        return (SimplePipelineBuilder<I>) super.registerSink(sinks, async);
    }

    @Override
    public SimplePipelineBuilder<I> setSinkExecutorProvider(Supplier<ExecutorService> sinkExecutorProvider)
    {
        return (SimplePipelineBuilder<I>) super.setSinkExecutorProvider(sinkExecutorProvider);
    }

    @Override
    public SimplePipelineBuilder<I> setSinkExecutor(ExecutorService sinkExecutor)
    {
        return (SimplePipelineBuilder<I>) super.setSinkExecutor(sinkExecutor);
    }

    @Override
    public SimplePipelineBuilder<I> setCloseTimeout(int closeTimeout)
    {
        return (SimplePipelineBuilder<I>) super.setCloseTimeout(closeTimeout);
    }

    @Override
    public SimplePipelineBuilder<I> setDefaultEvaluator(ResultEvaluator defaultEvaluator)
    {
        return (SimplePipelineBuilder<I>) super.setDefaultEvaluator(defaultEvaluator);
    }

    @Override
    public SimplePipelineBuilder<I> setDefaultErrorHandler(StepErrorHandler defaultErrorHandler)
    {
        return (SimplePipelineBuilder<I>) super.setDefaultErrorHandler(defaultErrorHandler);
    }
}
