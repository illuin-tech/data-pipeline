package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LatestMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Latest;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Latest latest = (Latest) category;
        boolean filterByName = latest.name() != null && !latest.name().isBlank();

        if (Result.class.isAssignableFrom(parameter.getType()))
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> getResults(args, latest).latest(latest.name()).orElseThrow();
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, latest).latest((Class<Result>) parameter.getType()).orElseThrow();
        }

        Optional<Class<? extends Result>> optionalArg = Reflection.getOptionalParameter(parameter, Result.class);
        if (optionalArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> getResults(args, latest).latest(latest.name());
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, latest).latest(optionalArg.get());
        }

        Optional<Class<? extends Result>> streamArg = Reflection.getStreamParameter(parameter, Result.class);
        if (streamArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> getResults(args, latest).stream().filter(r -> latest.name().equals(r.name()));
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, latest).stream().filter(r -> streamArg.get().isInstance(r));
        }

        throw new IllegalStateException("@Latest can only be assigned to a Result subtype or an Optional or Stream of a Result subtype");
    }

    private static Results getResults(MethodArguments<?, ?> args, Latest latest)
    {
        if (latest.self())
            return args.resultView().self();
        return args.results();
    }
}
