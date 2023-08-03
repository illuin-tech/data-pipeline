package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Payload;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PayloadMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Payload;
    }

    @Override
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
    {
        return MethodArguments::payload;
    }
}
