package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.annotation.Context;
import tech.illuin.pipeline.annotation.Latest;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.step.result.Result;

import javax.lang.model.type.PrimitiveType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ContextKeyMapperFactory<T, I, P> implements MethodArgumentMapperFactory<T, I, P>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return category instanceof Context;
    }

    @Override
    public MethodArgumentMapper<T, I, P> produce(Annotation category, Parameter parameter)
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
