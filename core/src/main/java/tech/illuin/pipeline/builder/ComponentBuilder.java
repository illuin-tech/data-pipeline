package tech.illuin.pipeline.builder;

import tech.illuin.pipeline.builder.runner_compiler.GenericRunnerCompiler;
import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;
import tech.illuin.pipeline.builder.runner_compiler.RunnerCompiler;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.MethodArgumentResolver;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public abstract class ComponentBuilder<T, I, CD, CC extends Annotation>
{
    protected final RunnerCompiler<CC, T, I> compiler;
    private final Class<CC> configType;

    protected ComponentBuilder(Class<CC> configType, MethodArgumentResolver<T, I> methodArgumentResolver, MethodValidator methodValidator)
    {
        this.compiler = new GenericRunnerCompiler<>(configType, methodArgumentResolver, methodValidator);
        this.configType = configType;
    }

    protected Optional<CC> checkAnnotation(Object component)
    {
        var type = component.getClass();
        if (!type.isAnnotationPresent(this.configType))
            return Optional.empty();

        var annotation = type.getAnnotation(this.configType);

        return Optional.of(annotation);
    }

    protected abstract void fillFromAnnotation(CC annotation);

    protected abstract void fillDefaults();

    protected abstract CD build();
}
