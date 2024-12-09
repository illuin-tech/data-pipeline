package tech.illuin.pipeline.generic.pipeline.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.MultiResult;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.variant.IndexableStep;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@StepConfig(id = "test-multi-step")
public class TestMultiStep<T extends Indexable> implements IndexableStep<T>
{
    private final String name;
    private final Function<T, List<String>> function;

    private static final Logger logger = LoggerFactory.getLogger(TestMultiStep.class);

    public TestMultiStep(String name, Function<T, List<String>> function)
    {
        this.name = name;
        this.function = function;
    }

    @Override
    public MultiResult execute(T data, ResultView results, Context context)
    {
        logger.info("test:{}: {}", this.name, data);
        List<String> values = this.function.apply(data);
        return MultiResult.of(values.stream().map(v -> new TestResult(this.name, v)).toList());
    }
}
