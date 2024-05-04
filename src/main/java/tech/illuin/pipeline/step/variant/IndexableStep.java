package tech.illuin.pipeline.step.variant;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface IndexableStep<T extends Indexable> extends Step<T, Object>
{
    Result execute(T object, ResultView results, Context context) throws Exception;

    /**
     * @see #execute(Indexable, ResultView, Context)
     */
    default Result execute(T object, Object input, Object payload, ResultView results, Context context) throws Exception
    {
        return this.execute(object, results, context);
    }

    /**
     * @see #execute(Indexable, ResultView, Context)
     */
    default Result execute(T object, Output output) throws Exception
    {
        return this.execute(object, output.results().view(object), output.context());
    }
}
