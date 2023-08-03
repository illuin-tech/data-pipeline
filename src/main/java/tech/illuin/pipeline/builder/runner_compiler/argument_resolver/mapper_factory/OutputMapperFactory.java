package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.output.Output;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class OutputMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return Output.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.output().getClass()))
                throw new IllegalArgumentException("This component expects the output to be a subtype of " + parameterType.getSimpleName() + " actual output is of type " + args.output().getClass().getSimpleName());
            return args.output();
        };
    }
}
