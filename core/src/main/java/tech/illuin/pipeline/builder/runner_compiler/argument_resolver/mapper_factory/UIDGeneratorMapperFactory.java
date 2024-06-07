package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class UIDGeneratorMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return UIDGenerator.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.uidGenerator().getClass()))
                throw new IllegalArgumentException("This component expects the UID generator to be a subtype of " + parameterType.getSimpleName() + " actual generator is of type " + args.uidGenerator().getClass().getSimpleName());
            return args.uidGenerator();
        };
    }
}
