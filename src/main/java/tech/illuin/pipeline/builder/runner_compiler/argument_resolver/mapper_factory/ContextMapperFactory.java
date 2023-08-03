package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.context.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ContextMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return Context.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.context().getClass()))
                throw new IllegalArgumentException("This component expects the context to be a subtype of " + parameterType.getSimpleName() + " actual tag is of type " + args.context().getClass().getSimpleName());
            return args.context();
        };
    }
}
