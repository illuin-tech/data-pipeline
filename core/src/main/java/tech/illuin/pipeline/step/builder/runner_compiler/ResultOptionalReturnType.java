package tech.illuin.pipeline.step.builder.runner_compiler;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.step.result.Result;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class ResultOptionalReturnType implements MethodValidator
{
    @Override
    public boolean validate(Method method)
    {
        if (!Optional.class.isAssignableFrom(method.getReturnType()))
            return false;

        Type genericReturnType = method.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType))
            return false;

        if (parameterizedType.getActualTypeArguments().length != 1)
            return false;

        Class<?> argumentType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        return Result.class.isAssignableFrom(argumentType);
    }

    @Override
    public String description()
    {
        return "The return value of a step method has to be a Optional<? extends Result> subtype";
    }
}
