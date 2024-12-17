package tech.illuin.pipeline;

import tech.illuin.pipeline.annotation.Experimental;
import tech.illuin.pipeline.builder.PayloadPipelineBuilder;
import tech.illuin.pipeline.builder.SimplePipelineBuilder;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.observer.descriptor.DescriptionNotAvailableException;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.variant.PipelineStep;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The Pipeline interface is both the top-level type for Pipeline implementations, and the entrypoint for the creation of pipeline builders via the {@link Pipeline#of} methods.
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface Pipeline<I> extends AutoCloseable
{
    /**
     * The returned identifier should ideally be unique throughout an application.
     * It is used in log entries and by the {@link PipelineContainer} as an identity and indexing key.
     *
     * @return the Pipeline's identifier
     */
    default String id()
    {
        return this.getClass().getName();
    }

    /**
     * The run method is the Pipeline's sole entrypoint, responsible for implementing its behaviour.
     * It returns an {@link Output} which is the composition of {@link Result} instances produced during the run itself, as well as eventual historical {@link Result} retrieved
     * from the {@link Context} at initialization.
     *
     * @param input an input value which can be directly processed by {@link Step} or can contribute to the initialization of a payload
     * @param context an optional {@link Context} instance providing information related tu current and eventual previous runs
     * @return the Pipeline's output
     * @throws PipelineException
     */
    Output run(I input, Context context) throws PipelineException;

    /**
     * Runs the Pipeline with a {@link SimpleContext} that can be adjusted via the setup argument.
     *
     * @see #run(Object, Context)
     */
    default Output run(I input, Consumer<Context> setup) throws PipelineException
    {
        Context context = new SimpleContext();
        setup.accept(context);
        return this.run(input, context);
    }

    /**
     * Runs the Pipeline with a null input and an empty {@link SimpleContext}.
     *
     * @see #run(Object, Context)
     */
    default Output run() throws PipelineException
    {
        return this.run(null);
    }

    /**
     * Runs the Pipeline with an empty {@link SimpleContext}.
     *
     * @see #run(Object, Context)
     */
    default Output run(I input) throws PipelineException
    {
        return this.run(input, new SimpleContext());
    }

    @Override
    default void close() throws Exception {}

    /**
     * @see #asStep(Function)
     */
    @Experimental
    default Step<?, I> asStep()
    {
        return this.asStep(PipelineResult::new);
    }

    /**
     * Returns a {@link Step} wrapping the execution of this Pipeline instance.
     * The wrapper is responsible for managing context continuity, making it possible for the pipeline to access upstream results.
     *
     * @param resultMapper a mapping function for transforming the pipeline {@link Output} into a valid {@link Result}
     * @return a {@link Step} instance usable within another Pipeline
     */
    @Experimental
    default Step<?, I> asStep(Function<Output, Result> resultMapper)
    {
        return new PipelineStep<>(this, resultMapper);
    }

    /* Builder providers */

    /**
     * Returns a new {@link SimplePipelineBuilder} instance for assembling a Pipeline which abstracts away payload management.
     *
     * @param id the Pipeline's identifier
     * @return the builder instance
     * @param <I> the Pipeline's input type
     */
    static <I> SimplePipelineBuilder<I> of(String id)
    {
        return new SimplePipelineBuilder<I>().setId(id);
    }

    /**
     * Returns a new {@link PayloadPipelineBuilder} instance, this is the default type of pipeline with input and payload management.
     *
     * @param id the Pipeline's identifier
     * @param initializer the Initializer responsible for creating the payload
     * @return the builder instance
     * @param <I> the Pipeline's input type
     * @see #of(String)
     */
    static <I> PayloadPipelineBuilder<I> of(String id, Initializer<I> initializer)
    {
        return new PayloadPipelineBuilder<I>()
            .setId(id)
            .setInitializer(initializer)
        ;
    }

    /**
     * @see #of(String, Initializer)
     */
    static <I> PayloadPipelineBuilder<I> of(String id, InitializerAssembler<I> initializer)
    {
        return new PayloadPipelineBuilder<I>()
            .setId(id)
            .setInitializer(initializer)
        ;
    }

    default Context newContext()
    {
        return new SimpleContext();
    }

    default Context newContext(Output parent)
    {
        return new SimpleContext(parent);
    }

    default PipelineDescription describe()
    {
        throw new DescriptionNotAvailableException("Not implemented");
    }
}
