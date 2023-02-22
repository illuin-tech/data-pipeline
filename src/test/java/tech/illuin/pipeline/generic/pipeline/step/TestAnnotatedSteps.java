package tech.illuin.pipeline.generic.pipeline.step;

import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.execution.condition.IsTypeB;
import tech.illuin.pipeline.generic.pipeline.execution.error.WrapAll;
import tech.illuin.pipeline.generic.pipeline.execution.evaluator.AbortOnStatusKo;
import tech.illuin.pipeline.generic.pipeline.execution.evaluator.DiscardOnStatusKo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.variant.IndexableStep;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestAnnotatedSteps
{
    @StepConfig(id = "test-step-condition", condition = IsTypeB.class)
    public static class ConditionActivation<T extends Indexable> extends AnnotatedStep<T>
    {
        public ConditionActivation(String name, Function<T, String> function) { super(name, function); }
        public ConditionActivation(String name, String status) { super(name, status); }
    }

    @StepConfig(id = "test-step-class-condition", conditionOnClass = A.class)
    public static class ClassActivation<T extends Indexable> extends AnnotatedStep<T>
    {
        public ClassActivation(String name, Function<T, String> function) { super(name, function); }
        public ClassActivation(String name, String status) { super(name, status); }
    }

    @StepConfig(id = "test-step-evaluator", evaluator = AbortOnStatusKo.class)
    public static class Evaluator<T extends Indexable> extends AnnotatedStep<T>
    {
        public Evaluator(String name, Function<T, String> function) { super(name, function); }
        public Evaluator(String name, String status) { super(name, status); }
    }

    @StepConfig(id = "test-step-error-handler", errorHandler = WrapAll.class, evaluator = DiscardOnStatusKo.class)
    public static class ErrorHandler<T extends Indexable> extends AnnotatedStep<T>
    {
        public ErrorHandler(String name, Function<T, String> function) { super(name, function); }
        public ErrorHandler(String name, String status) { super(name, status); }
    }

    private abstract static class AnnotatedStep<T extends Indexable> implements IndexableStep<T>
    {
        private final String name;
        private final Function<T, String> function;

        private static final Logger logger = LoggerFactory.getLogger(AnnotatedStep.class);

        public AnnotatedStep(String name, Function<T, String> function)
        {
            this.name = name;
            this.function = function;
        }

        public AnnotatedStep(String name, String status)
        {
            this(name, in -> status);
        }

        @Override
        public Result execute(T data, ResultView results, Context<?> context)
        {
            logger.info("test:{}: {}", this.name, data);
            return new TestResult(this.name, this.function.apply(data));
        }
    }
}
