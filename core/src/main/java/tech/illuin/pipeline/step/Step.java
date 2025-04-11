package tech.illuin.pipeline.step;

import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.runner.StepRunner;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Step<T extends Indexable, I>
{
    Result execute(T object, I input, Object payload, ResultView results, LocalContext context) throws Exception;

    default Result execute(T object, I input, Output output, LocalContext context) throws Exception
    {
        return this.execute(object, input, output.payload(Object.class), output.results().view(object), context);
    }

    default String defaultId()
    {
        return Reflection.isAnonymousImplementation(this.getClass()) ? "anonymous-step" : this.getClass().getName();
    }

    /**
     * Create a {@link Step} out of a non-specific instance (i.e. not a {@link Step} implementation).
     * The resulting step will leverage reflection-based method introspection mechanisms for resolving execution configuration.
     */
    static <T extends Indexable, I> Step<T, I> of(Object target)
    {
        return new StepRunner<>(target);
    }
}
