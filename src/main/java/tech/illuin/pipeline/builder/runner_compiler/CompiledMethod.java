package tech.illuin.pipeline.builder.runner_compiler;

import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record CompiledMethod<C extends Annotation, T, I, P>(
    Method method,
    List<MethodArgumentMapper<T, I, P>> mappers,
    C config
) {}
