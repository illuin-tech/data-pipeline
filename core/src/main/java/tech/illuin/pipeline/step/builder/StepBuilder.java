package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.builder.ComponentBuilder;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.execution.condition.StepCondition;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.wrapper.StepWrapper;
import tech.illuin.pipeline.step.runner.StepRunner;
import tech.illuin.pipeline.step.variant.IndexableStep;
import tech.illuin.pipeline.step.variant.InputStep;
import tech.illuin.pipeline.step.variant.PayloadStep;
import tech.illuin.pipeline.step.variant.PipelineStep;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepBuilder<T extends Indexable, I> extends ComponentBuilder<T, I, StepDescriptor<T, I>, StepConfig>
{
    private String id;
    private Step<T, I> step;
    private Boolean pinned;
    private StepWrapper<T, I> executionWrapper;
    private StepCondition executionCondition;
    private ResultEvaluator resultEvaluator;
    private StepErrorHandler errorHandler;

    public StepBuilder()
    {
        super(StepConfig.class, new StepMethodArgumentResolver<>(), StepMethodValidators.validators);
    }

    @Override
    protected void fillFromAnnotation(StepConfig annotation)
    {
        try {
            if (this.id == null && annotation.id() != null && !annotation.id().isBlank())
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

    @Override
    protected void fillDefaults()
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
    public StepBuilder<T, I> step(Step<? extends T, I> step)
    {
        this.step = (Step<T, I>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I> step(IndexableStep<? extends T> step)
    {
        this.step = (Step<T, I>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I> step(InputStep<I> step)
    {
        this.step = (Step<T, I>) step;
        return this;
    }

    @SuppressWarnings("unchecked")
    public StepBuilder<T, I> step(PayloadStep step)
    {
        this.step = (Step<T, I>) step;
        return this;
    }

    public StepBuilder<T, I> step(Object target)
    {
        if (target instanceof PipelineStep<?> pipelineStep)
            //noinspection unchecked
            this.step = (Step<T, I>) pipelineStep;
        else
            this.step = Step.of(target);
        return this;
    }

    public StepBuilder<T, I> withId(String id)
    {
        this.id = id;
        return this;
    }

    public StepBuilder<T, I> withWrapper(StepWrapper<T, I> wrapper)
    {
        this.executionWrapper = wrapper;
        return this;
    }

    public StepBuilder<T, I> withCondition(Class<? extends Indexable> typeCondition)
    {
        this.executionCondition = (idx, ctx) -> typeCondition.isInstance(idx);
        return this;
    }

    public StepBuilder<T, I> withCondition(StepCondition predicate)
    {
        this.executionCondition = predicate;
        return this;
    }

    public StepBuilder<T, I> withEvaluation(ResultEvaluator evaluator)
    {
        this.resultEvaluator = evaluator;
        return this;
    }

    public StepBuilder<T, I> withErrorHandler(StepErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    public StepBuilder<T, I> setPinned(boolean pin)
    {
        this.pinned = pin;
        return this;
    }

    @Override
    protected StepDescriptor<T, I> build()
    {
        if (this.step == null)
            throw new IllegalStateException("A StepDescriptor cannot be built from a null step");

        Optional<StepConfig> config;
        if (this.step instanceof StepRunner<T, I> stepRunner)
        {
            CompiledMethod<StepConfig, T, I> compiled = this.compiler.compile(stepRunner.target());
            stepRunner.build(compiled);
            config = Optional.of(compiled.config());
        }
        else
            config = this.checkAnnotation(this.step);

        config.ifPresent(this::fillFromAnnotation);

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
