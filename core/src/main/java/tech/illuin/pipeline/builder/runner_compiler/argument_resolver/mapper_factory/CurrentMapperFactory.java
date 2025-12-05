package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Current;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MissingArgument;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class CurrentMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Current current = (Current) category;
        boolean filterByName = current.name() != null && !current.name().isBlank();

        if (Result.class.isAssignableFrom(parameter.getType()))
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
            {
                return args -> getResults(args, current)
                    .current(current.name())
                    .or(() -> Optional.of(new MissingArgument("@Current argument with name " + current.name() + " is missing but not marked as required")).filter(ma -> !current.required()))
                    .orElseThrow(() -> new NoSuchElementException("@Current argument with type " + current.name() + " is missing"));
            }
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, current)
                .current((Class<Result>) parameter.getType())
                .or(() -> Optional.of(new MissingArgument("@Current argument with type " + parameter.getType() + " is missing but not marked as required")).filter(ma -> !current.required()))
                .orElseThrow(() -> new NoSuchElementException("@Current argument with type " + parameter.getType() + " is missing"));
        }

        Optional<Class<? extends Result>> optionalArg = Reflection.getOptionalParameter(parameter, Result.class);
        if (optionalArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> getResults(args, current).current(current.name());
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, current).current(optionalArg.get());
        }

        Optional<Class<? extends Result>> streamArg = Reflection.getStreamParameter(parameter, Result.class);
        if (streamArg.isPresent())
        {
            /* If a name was specified, we rely on it for filtering */
            if (filterByName)
                return args -> getResults(args, current).current().filter(r -> current.name().equals(r.name()));
            /* Otherwise, we filter by argument type */
            return args -> getResults(args, current).current().filter(r -> streamArg.get().isInstance(r));
        }

        throw new IllegalStateException("@Current can only be assigned to a Result subtype or an Optional or Stream of a Result subtype");
    }

    private static Results getResults(MethodArguments<?, ?> args, Current current)
    {
        if (current.self())
            return args.resultView().self();
        return args.results();
    }
}
