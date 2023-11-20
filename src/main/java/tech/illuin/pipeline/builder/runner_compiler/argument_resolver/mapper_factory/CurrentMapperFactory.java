package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.step.result.Result;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CurrentMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
    {
        Current current = (Current) category;
        boolean filterByName = current.name() != null && !current.name().isBlank();

        if (Result.class.isAssignableFrom(parameter.getType()))
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> args.results().current(current.name()).orElseThrow();
            /* Otherwise, we filter by argument type */
            return args -> args.results().current((Class<Result>) parameter.getType()).orElseThrow();
        }

        Optional<Class<? extends Result>> optionalArg = Reflection.getOptionalParameter(parameter, Result.class);
        if (optionalArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> args.results().current(current.name());
            /* Otherwise, we filter by argument type */
            return args -> args.results().current(optionalArg.get());
        }

        Optional<Class<? extends Result>> streamArg = Reflection.getStreamParameter(parameter, Result.class);
        if (streamArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> args.results().current().filter(r -> current.name().equals(r.name()));
            /* Otherwise, we filter by argument type */
            return args -> args.results().current().filter(r -> streamArg.get().isInstance(r));
        }

        throw new IllegalStateException("@Current can only be assigned to a Result subtype or an Optional or Stream of a Result subtype");
    }
}
