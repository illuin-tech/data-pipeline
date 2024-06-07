package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.metering.marker.LogMarker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LogMarkerMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return LogMarker.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.logMarker().getClass()))
                throw new IllegalArgumentException("This component expects the log marker to be a subtype of " + parameterType.getSimpleName() + " actual generator is of type " + args.logMarker().getClass().getSimpleName());
            return args.logMarker();
        };
    }
}
