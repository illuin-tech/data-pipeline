package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.Step;
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
public final class StepDescriptor<T extends Indexable, I>
{
    private final String id;
    private final Step<T, I> step;
    private final boolean pinned;
    private final StepWrapper<T, I> executionWrapper;
    private final StepCondition activationPredicate;
    private final ResultEvaluator resultEvaluator;
    private final StepErrorHandler errorHandler;

    StepDescriptor(
        String id,
        Step<T, I> step,
        boolean pinned,
        StepWrapper<T, I> executionWrapper,
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

    public Result execute(T data, I input, Output output, LocalContext ctx) throws Exception
    {
        return this.executionWrapper.wrap(this.step).execute(data, input, output, ctx);
    }

    public boolean canExecute(Indexable indexable, Context ctx)
    {
        return this.activationPredicate.canExecute(indexable, ctx);
    }

    public StepStrategy postEvaluation(Result result, Indexable object, I input, LocalContext ctx)
    {
        return this.resultEvaluator.evaluate(result, object, input, ctx);
    }

    public Result handleException(Exception ex, I input, Object payload, Results results, LocalContext ctx) throws Exception
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

    public Step<T, I> step()
    {
        return this.step;
    }

    public StepWrapper<T, I> executionWrapper()
    {
        return this.executionWrapper;
    }

    public StepCondition activationPredicate()
    {
        return this.activationPredicate;
    }

    public ResultEvaluator resultEvaluator()
    {
        return this.resultEvaluator;
    }

    public StepErrorHandler errorHandler()
    {
        return this.errorHandler;
    }
}
