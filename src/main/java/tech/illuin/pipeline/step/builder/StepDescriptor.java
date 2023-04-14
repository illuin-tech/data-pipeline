package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.StepException;
import tech.illuin.pipeline.step.execution.condition.StepCondition;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class StepDescriptor<T extends Indexable, I, P>
{
    private final String id;
    private final Step<T, I, P> step;
    private final boolean pinned;
    private final StepWrapper<T, I, P> executionWrapper;
    private final StepCondition activationPredicate;
    private final ResultEvaluator resultEvaluator;
    private final StepErrorHandler errorHandler;

    StepDescriptor(
        String id,
        Step<T, I, P> step,
        boolean pinned,
        StepWrapper<T, I, P> executionWrapper,
        StepCondition activationPredicate,
        ResultEvaluator resultEvaluator,
        StepErrorHandler errorHandler
    ) {
        this.id = id;
        this.step = step;
        this.pinned = pinned;
        this.executionWrapper = executionWrapper;
        this.activationPredicate = activationPredicate;
        this.resultEvaluator = resultEvaluator;
        this.errorHandler = errorHandler;
    }

    public Result execute(T data, I input, Output<P> output) throws StepException
    {
        return this.executionWrapper.wrap(this.step).execute(data, input, output);
    }

    public boolean canExecute(Indexable indexable, Context<P> ctx)
    {
        return this.activationPredicate.canExecute(indexable, ctx);
    }

    public StepStrategy postEvaluation(Result result, Indexable object, I input, Context<P> ctx)
    {
        return this.resultEvaluator.evaluate(result, object, input, ctx);
    }

    public Result handleException(Exception ex, I input, P payload, Results results, Context<P> ctx) throws StepException
    {
        return this.errorHandler.handle(ex, input, payload, results, ctx);
    }

    public String id()
    {
        return this.id;
    }

    public boolean isPinned()
    {
        return this.pinned;
    }
}
