package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.step.builder.runner_compiler.ResultCollectionReturnType;
import tech.illuin.pipeline.step.builder.runner_compiler.ResultReturnType;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class StepMethodValidators
{
    public static final MethodValidator validator = MethodValidator.or(
        new ResultReturnType(),
        new ResultCollectionReturnType()
    );

    private StepMethodValidators() {}
}
