package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.input.initializer.builder.runner_compiler.NonVoidReturnType;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class InitializerMethodValidators
{
    public static final MethodValidator validator = MethodValidator.and(
        new NonVoidReturnType()
    );

    private InitializerMethodValidators() {}
}
