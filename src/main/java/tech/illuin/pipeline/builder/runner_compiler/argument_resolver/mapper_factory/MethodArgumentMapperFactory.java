package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MethodArgumentMapperFactory<T, I, P>
{
    boolean canHandle(Annotation category, Class<?> parameterType);

    MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter);
}
