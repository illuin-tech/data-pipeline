package tech.illuin.pipeline.resilience4j.execution.wrapper.config;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.result.ResultView;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public interface StepHandler
{
    <T extends Indexable, I> void onError(T object, I input, Object payload, ResultView view, Context context, Exception e);

    <T extends Indexable, I> void onSuccess(T object, I input, Object payload, ResultView view, Context context);
}
