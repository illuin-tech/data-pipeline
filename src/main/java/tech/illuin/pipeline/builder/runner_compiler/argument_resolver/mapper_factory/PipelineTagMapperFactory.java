package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.output.PipelineTag;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineTagMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return PipelineTag.class.isAssignableFrom(parameterType);
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.pipelineTag().getClass()))
                throw new IllegalArgumentException("This component expects the pipeline tag to be a subtype of " + parameterType.getSimpleName() + " actual tag is of type " + args.pipelineTag().getClass().getSimpleName());
            return args.pipelineTag();
        };
    }
}
