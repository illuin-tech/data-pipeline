package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.step.result.Result;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class StepMethodValidators
{
    public static final List<MethodValidator> validators = List.of(
        StepMethodValidators::hasAResultReturnType
    );

    private StepMethodValidators() {}

    public static void hasAResultReturnType(Method method)
    {
        if (!Result.class.isAssignableFrom(method.getReturnType()))
            throw new IllegalStateException("The return value of a step method has to be a Result subtype");
    }
}
