package tech.illuin.pipeline.step;

import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

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
}
