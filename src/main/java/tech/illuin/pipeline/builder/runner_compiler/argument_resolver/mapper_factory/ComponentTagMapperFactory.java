package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.output.ComponentTag;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ComponentTagMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return ComponentTag.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.componentTag().getClass()))
                throw new IllegalArgumentException("This component expects the component tag to be a subtype of " + parameterType.getSimpleName() + " actual generator is of type " + args.componentTag().getClass().getSimpleName());
            return args.componentTag();
        };
    }
}
