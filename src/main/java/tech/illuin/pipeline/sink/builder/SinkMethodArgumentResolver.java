package tech.illuin.pipeline.sink.builder;

import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.annotation.Input;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.annotation.Payload;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.MethodArgumentResolver;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
class SinkMethodArgumentResolver<T, I, P> implements MethodArgumentResolver<T, I, P>
{
    private final List<MethodArgumentMapperFactory<T, I, P>> factories;

    private static final Set<Class<?>> LEGAL_ANNOTATIONS = Set.of(
        Input.class, Payload.class, Current.class, Latest.class
    );

    SinkMethodArgumentResolver()
    {
        this.factories = List.of(
            new InputMapperFactory<>(),
            new PayloadMapperFactory<>(),
            new OutputMapperFactory<>(),
            new CurrentMapperFactory<>(),
            new LatestMapperFactory<>(),
            new ContextMapperFactory<>(),
            new ResultsMapperFactory<>(),
            new UIDGeneratorMapperFactory<>(),
            new PipelineTagMapperFactory<>(),
            new ComponentTagMapperFactory<>()
        );
    }

    @Override
    public MethodArgumentMapper<T, I, P> resolveMapper(Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        List<Annotation> parameterAnnotations = Stream.of(parameter.getDeclaredAnnotations())
            .filter(a -> ANNOTATIONS.contains(a.annotationType()))
            .toList()
        ;

        if (parameterAnnotations.size() > 1)
            throw new IllegalStateException("There can only be one of " + LEGAL_ANNOTATIONS + " on a sink method");
        if (parameterAnnotations.size() == 1 && !LEGAL_ANNOTATIONS.contains(parameterAnnotations.get(0).annotationType()))
            throw new IllegalStateException("There can only be one of " + LEGAL_ANNOTATIONS + " on a sink method");

        Annotation category = parameterAnnotations.isEmpty() ? null : parameterAnnotations.get(0);

        for (var factory : this.factories)
        {
            if (factory.canHandle(category, parameterType))
                return factory.produce(category, parameter);
        }

        throw new IllegalStateException("No matching mapper factory could be found for parameter " + parameter.getName() + " of type " + parameter.getType().getSimpleName());
    }
}
