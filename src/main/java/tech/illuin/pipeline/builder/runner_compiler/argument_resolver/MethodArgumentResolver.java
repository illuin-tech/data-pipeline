package tech.illuin.pipeline.builder.runner_compiler.argument_resolver;

import tech.illuin.pipeline.annotation.*;
import tech.illuin.pipeline.annotation.Object;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;

import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MethodArgumentResolver<T, I>
{
    Set<Class<?>> ANNOTATIONS = Set.of(
        Input.class, Object.class, Payload.class, Current.class, Latest.class, Context.class
    );

    MethodArgumentMapper<T, I> resolveMapper(Parameter parameter);
}
