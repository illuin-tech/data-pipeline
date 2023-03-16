package tech.illuin.pipeline;

import tech.illuin.pipeline.annotation.Experimental;
import tech.illuin.pipeline.builder.PayloadPipelineBuilder;
import tech.illuin.pipeline.builder.SimplePipelineBuilder;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.builder.InitializerAssembler;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.variant.PipelineStep;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface Pipeline<I, P> extends AutoCloseable
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
    Output<P> run(I input, Context<P> context) throws PipelineException;

    /**
     * Runs the Pipeline with a {@link SimpleContext} that can be adjusted via the setup argument.
     *
     * @see #run(Object, Context)
     */
    default Output<P> run(I input, Consumer<Context<P>> setup) throws PipelineException
    {
        Context<P> context = new SimpleContext<>();
        setup.accept(context);
        return this.run(input, context);
    }

    /**
     * Runs the Pipeline with a null input and an empty {@link SimpleContext}.
     *
     * @see #run(Object, Context)
     */
    default Output<P> run() throws PipelineException
    {
        return this.run(null);
    }

    /**
     * Runs the Pipeline with an empty {@link SimpleContext}.
     *
     * @see #run(Object, Context)
     */
    default Output<P> run(I input) throws PipelineException
    {
        return this.run(input, new SimpleContext<>());
    }

    @Override
    default void close() throws Exception {}

    /**
     * @see #asStep(Function)
     */
    @Experimental
    default Step<?, I, VoidPayload> asStep()
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
    default Step<?, I, VoidPayload> asStep(Function<Output<P>, Result> resultMapper)
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
    static <I> SimplePipelineBuilder<I> ofSimple(String id)
    {
        return new SimplePipelineBuilder<I>().setId(id);
    }

    /**
     * Returns a new {@link PayloadPipelineBuilder} instance, this is the default type of pipeline with input and payload management.
     *
     * @param id the Pipeline's identifier
     * @return the builder instance
     * @param <I> the Pipeline's input type
     * @param <P> the Pipeline's payload type
     */
    static <I, P> PayloadPipelineBuilder<I, P> ofPayload(String id)
    {
        return new PayloadPipelineBuilder<I, P>().setId(id);
    }

    /**
     * @see #ofPayload(String)
     */
    static <I, P> PayloadPipelineBuilder<I, P> ofPayload(String id, Initializer<I, P> initializer)
    {
        return new PayloadPipelineBuilder<I, P>()
            .setId(id)
            .setInitializer(initializer)
        ;
    }

    /**
     * @see #ofPayload(String)
     */
    static <I, P> PayloadPipelineBuilder<I, P> ofPayload(String id, InitializerAssembler<I, ? extends P> initializer)
    {
        return new PayloadPipelineBuilder<I, P>()
            .setId(id)
            .setInitializer(initializer)
        ;
    }
}
