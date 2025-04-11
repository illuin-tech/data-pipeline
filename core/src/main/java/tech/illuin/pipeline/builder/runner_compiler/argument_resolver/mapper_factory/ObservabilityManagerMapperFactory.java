package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.metering.manager.ObservabilityManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ObservabilityManagerMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return ObservabilityManager.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.observabilityManager().getClass()))
                throw new IllegalArgumentException("This component expects the observability manager to be a subtype of " + parameterType.getSimpleName() + " actual manager is of type " + args.observabilityManager().getClass().getSimpleName());
            return args.observabilityManager();
        };
    }
}
