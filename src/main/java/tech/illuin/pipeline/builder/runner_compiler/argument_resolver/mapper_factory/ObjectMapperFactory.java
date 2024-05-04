package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ObjectMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Object;
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        return MethodArguments::object;
    }
}
