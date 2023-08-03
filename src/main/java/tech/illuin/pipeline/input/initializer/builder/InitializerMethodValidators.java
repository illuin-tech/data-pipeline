package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class InitializerMethodValidators
{
    public static final List<MethodValidator> validators = List.of(
        InitializerMethodValidators::hasANonVoidReturnType
    );

    private InitializerMethodValidators() {}

    public static void hasANonVoidReturnType(Method method)
    {
        if (method.getReturnType().equals(void.class))
            throw new IllegalStateException("The return value of an initializer method cannot be void");
    }
}
