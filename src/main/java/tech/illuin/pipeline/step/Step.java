package tech.illuin.pipeline.step;

import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.runner.StepRunner;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Step<T extends Indexable, I, P>
{
    Result execute(T object, I input, P payload, ResultView results, Context<P> context) throws Exception;

    default Result execute(T object, I input, Output<P> output, Context<P> context) throws Exception
    {
        return this.execute(object, input, output.payload(), output.results().view(object), context);
    }

    default String defaultId()
    {
        return Reflection.isAnonymousImplementation(this.getClass()) ? "anonymous-step" : this.getClass().getName();
    }

    /**
     * Create a {@link Step} out of a non-specific instance (i.e. not a {@link Step} implementation).
     * The resulting step will leverage reflection-based method introspection mechanisms for resolving execution configuration.
     */
    static <T extends Indexable, I, P> Step<T, I, P> of(Object target)
    {
        return new StepRunner<>(target);
    }
}
