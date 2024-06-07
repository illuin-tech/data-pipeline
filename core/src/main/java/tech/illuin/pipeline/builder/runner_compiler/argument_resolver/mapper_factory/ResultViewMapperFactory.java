package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.step.result.ResultView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ResultViewMapperFactory<T, I> implements MethodArgumentMapperFactory<T, I>
{
    @Override
    public boolean canHandle(Annotation category, Class<?> parameterType)
    {
        return parameterType.isAssignableFrom(ResultView.class);
    }

    @Override
    public MethodArgumentMapper<T, I> produce(Annotation category, Parameter parameter)
    {
        Class<?> parameterType = parameter.getType();
        return args -> {
            if (!parameterType.isAssignableFrom(args.resultView().getClass()))
                throw new IllegalArgumentException("This step expects the results to be a subtype of " + parameterType.getSimpleName() + " actual result is of type " + args.results().getClass().getSimpleName());
            return args.resultView();
        };
    }
}
