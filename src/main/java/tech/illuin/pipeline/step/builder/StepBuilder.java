package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.execution.condition.StepCondition;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepBuilder<T extends Indexable, I, P>
{
    private String id;
    private Step<T, I, P> step;
    private Boolean pinned;
    private StepWrapper<T, I, P> executionWrapper;
    private StepCondition executionCondition;
    private ResultEvaluator resultEvaluator;
    private StepErrorHandler errorHandler;

    private void checkAnnotation()
    {
        try {
            var type = this.step.getClass();
            if (!type.isAnnotationPresent(StepConfig.class))
                return;

            var annotation = type.getAnnotation(StepConfig.class);

            if (this.executionWrapper == null && annotation.id() != null && !annotation.id().isBlank())
                this.id = annotation.id();
            if (this.pinned == null)
                this.pinned = annotation.pinned();
            if (this.executionCondition == null && annotation.condition() != null)
            {
                if (annotation.condition() != StepCondition.class)
                    this.executionCondition = annotation.condition().getConstructor().newInstance();
                else if (annotation.conditionOnClass() != Indexable.class)
                    this.executionCondition = (idx, ctx) -> annotation.conditionOnClass().isInstance(idx);
            }
            if (this.resultEvaluator == null && annotation.evaluator() != null && annotation.evaluator() != ResultEvaluator.class)
                this.resultEvaluator = annotation.evaluator().getConstructor().newInstance();
            if (this.errorHandler == null && annotation.errorHandler() != null && annotation.errorHandler() != StepErrorHandler.class)
                this.errorHandler = annotation.errorHandler().getConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while attempting to instantiate a StepConfig argument", e);
        }
    }

    private void fillDefaults()
    {
        if (this.id == null)
            this.id = this.step.defaultId();
        if (this.pinned == null)
            this.pinned = false;
        if (this.executionWrapper == null)
            this.executionWrapper = StepWrapper::noOp;
        if (this.executionCondition == null)
            this.executionCondition = StepCondition.ALWAYS_TRUE;
        if (this.resultEvaluator == null)
            this.resultEvaluator = ResultEvaluator.ALWAYS_CONTINUE;
        if (this.errorHandler == null)
            this.errorHandler = StepErrorHandler.RETHROW_ALL;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I, P> step(Step<? extends T, I, P> step)
    {
        this.step = (Step<T, I, P>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I, P> step(IndexableStep<? extends T> step)
    {
        this.step = (Step<T, I, P>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I, P> step(InputStep<I> step)
    {
        this.step = (Step<T, I, P>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I, P> step(PayloadStep<P> step)
    {
        this.step = (Step<T, I, P>) step;
        return this;
    }

    public StepBuilder<T, I, P> withId(String id)
    {
        this.id = id;
        return this;
    }

    public StepBuilder<T, I, P> withWrapper(StepWrapper<T, I, P> wrapper)
    {
        this.executionWrapper = wrapper;
        return this;
    }

    public StepBuilder<T, I, P> withCondition(Class<? extends Indexable> typeCondition)
    {
        this.executionCondition = (idx, ctx) -> typeCondition.isInstance(idx);
        return this;
    }

    public StepBuilder<T, I, P> withCondition(StepCondition predicate)
    {
        this.executionCondition = predicate;
        return this;
    }

    public StepBuilder<T, I, P> withEvaluation(ResultEvaluator evaluator)
    {
        this.resultEvaluator = evaluator;
        return this;
    }

    public StepBuilder<T, I, P> withErrorHandler(StepErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    public StepBuilder<T, I, P> setPinned(boolean pin)
    {
        this.pinned = pin;
        return this;
    }

    StepDescriptor<T, I, P> build()
    {
        if (this.step == null)
            throw new IllegalStateException("A StepDescriptor cannot be built from a null step");

        this.checkAnnotation();
        this.fillDefaults();

        return new StepDescriptor<>(
            this.id,
            this.step,
            this.pinned,
            this.executionWrapper,
            this.executionCondition,
            this.resultEvaluator,
            this.errorHandler
        );
    }
}