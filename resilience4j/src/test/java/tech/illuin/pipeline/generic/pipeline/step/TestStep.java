package tech.illuin.pipeline.generic.pipeline.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.variant.IndexableStep;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@StepConfig(id = "test-step")
public class TestStep<T extends Indexable> implements IndexableStep<T>
{
    private final String name;
    private final Action<T, String> function;

    private static final Logger logger = LoggerFactory.getLogger(TestStep.class);

    public TestStep(String name, Action<T, String> function)
    {
        this.name = name;
        this.function = function;
    }

    public TestStep(String name, String status)
    {
        this(name, in -> status);
    }

    @Override
    public Result execute(T data, ResultView results, Context context) throws Exception
    {
        logger.info("test:{}: {}", this.name, data);
        return new TestResult(this.name, this.function.apply(data));
    }

    @FunctionalInterface
    public interface Action<T, R>
    {
        R apply(T t) throws Exception;
    }
}
