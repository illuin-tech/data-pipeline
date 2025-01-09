package tech.illuin.pipeline.builder.runner_compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.MethodArgumentResolver;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.commons.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class GenericRunnerCompiler<C extends Annotation, T, I> implements RunnerCompiler<C, T, I>
{
    private final Class<C> configType;
    private final MethodArgumentResolver<T, I> argumentResolver;
    private final MethodValidator methodValidator;

    private static final Logger logger = LoggerFactory.getLogger(GenericRunnerCompiler.class);

    public GenericRunnerCompiler(Class<C> configType, MethodArgumentResolver<T, I> argumentResolver)
    {
        this(configType, argumentResolver, MethodValidator.any());
    }

    public GenericRunnerCompiler(Class<C> configType, MethodArgumentResolver<T, I> argumentResolver, MethodValidator methodValidator)
    {
        this.configType = configType;
        this.argumentResolver = argumentResolver;
        this.methodValidator = methodValidator;
    }

    @Override
    public CompiledMethod<C, T, I> compile(Object target)
    {
        logger.trace("{}: Compiling runner-based {} with target type {}", this.configType.getSimpleName(), this, target.getClass().getName());

        Class<?> c = target.getClass();
        List<MethodCandidate<C>> candidates = this.lookupStepMethods(c);

        if (candidates.isEmpty())
            throw new IllegalStateException("No candidate " + this.configType.getSimpleName() + " method could be found in target class " + c.getSimpleName());
        if (candidates.size() > 1)
            throw new IllegalStateException("There can only be one " + this.configType.getSimpleName() + " method per class.");

        MethodCandidate<C> candidate = candidates.get(0);
        logger.trace("{}: Found method candidate {}", this, Reflection.getMethodSignature(candidate.method()));
        if (!this.methodValidator.validate(candidate.method()))
            throw new IllegalStateException(this.methodValidator.description());

        return new CompiledMethod<>(
            candidate.method(),
            this.compileArguments(candidate),
            candidate.config()
        );
    }

    private List<MethodCandidate<C>> lookupStepMethods(Class<?> c)
    {
        List<MethodCandidate<C>> candidates = new ArrayList<>();
        for (Method method : c.getDeclaredMethods())
        {
            C[] configs = method.getDeclaredAnnotationsByType(this.configType);
            if (configs.length > 0)
                candidates.add(new MethodCandidate<>(method, configs[0]));
        }
        return candidates;
    }

    private List<MethodArgumentMapper<T, I>> compileArguments(MethodCandidate<C> candidate)
    {
        List<MethodArgumentMapper<T, I>> mappers = new ArrayList<>();
        Parameter[] parameters = candidate.method().getParameters();
        for (Parameter parameter : parameters)
        {
            MethodArgumentMapper<T, I> mapper = this.argumentResolver.resolveMapper(parameter);
            mappers.add(mapper);
        }
        return mappers;
    }

    public record MethodCandidate<C extends Annotation>(
        Method method,
        C config
    ) {}
}
