package tech.illuin.pipeline.step.annotation;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.execution.condition.StepCondition;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface StepConfig
{
    String id() default "";
    String resultKey() default "";
    boolean pinned() default false;
    Class<? extends StepCondition> condition() default StepCondition.class;
    Class<? extends Indexable> conditionOnClass() default Indexable.class;
    Class<? extends ResultEvaluator> evaluator() default ResultEvaluator.class;
    Class<? extends StepErrorHandler> errorHandler() default StepErrorHandler.class;
}
