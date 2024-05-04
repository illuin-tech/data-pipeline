package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.commons.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ContextKeyMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Context;
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Context context = (Context) category;

        Optional<Class<?>> optionalArg = Reflection.getOptionalParameter(parameter, Object.class);
        if (optionalArg.isPresent())
            return args -> args.context().get(context.value(), optionalArg.get());

        Class<?> type = parameter.getType().isPrimitive()
            ? Reflection.getWrapperType(parameter.getType())
            : parameter.getType()
        ;

        return args -> args.context().get(context.value(), type).orElseThrow();
    }
}
