package tech.illuin.pipeline.step.annotation.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.step.annotation.StepConfig;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IllegalStepCollectionGenericMethod<T>
{
    private static final Logger logger = LoggerFactory.getLogger(IllegalStepCollectionGenericMethod.class);

    @StepConfig
    public List<Integer> execute(@Input T data)
    {
        logger.info("input: {}", data);
        return List.of(1, 2, 3);
    }
}
