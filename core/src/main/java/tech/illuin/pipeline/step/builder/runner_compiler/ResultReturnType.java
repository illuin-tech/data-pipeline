package tech.illuin.pipeline.step.builder.runner_compiler;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.step.result.Result;

import java.lang.reflect.Method;

public class ResultReturnType implements MethodValidator
{
    @Override
    public boolean validate(Method method)
    {
        return Result.class.isAssignableFrom(method.getReturnType());
    }

    @Override
    public String description()
    {
        return "The return value of a step method has to be a Result subtype";
    }
}
