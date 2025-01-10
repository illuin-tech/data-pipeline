package tech.illuin.pipeline.input.initializer.builder.runner_compiler;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;

import java.lang.reflect.Method;

public class NonVoidReturnType implements MethodValidator
{
    @Override
    public boolean validate(Method method)
    {
        return !method.getReturnType().equals(void.class);
    }

    @Override
    public String description()
    {
        return "The return value of an initializer method cannot be void";
    }
}
