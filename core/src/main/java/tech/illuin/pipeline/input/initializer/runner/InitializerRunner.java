package tech.illuin.pipeline.input.initializer.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.step.runner.StepRunnerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerRunner<I> implements Initializer<I>, Describable
{
    private final Object target;
    private Method method;
    private List<MethodArgumentMapper<Object, I>> argumentMappers;

    private static final Logger logger = LoggerFactory.getLogger(InitializerRunner.class);

    public InitializerRunner(Object target)
    {
        if (target == null)
            throw new IllegalArgumentException("The target of an InitializerRunner cannot be null");
        this.target = target;
    }

    @Override
    public Object initialize(I input, LocalContext context, UIDGenerator generator) throws Exception
    {
        logger.trace("{}#{} launching initializer over target {}#{}", context.pipelineTag().pipeline(), context.pipelineTag().uid(), this.target.getClass().getName(), Reflection.getMethodSignature(this.method));

        try {
            MethodArguments<Object, I> originalArguments = new MethodArguments<>(
                null,
                input,
                null,
                null,
                null,
                null,
                context,
                context.pipelineTag(),
                context.componentTag(),
                generator,
                context.markerManager()
            );
            Object[] arguments = this.argumentMappers.stream()
                .map(mapper -> mapper.map(originalArguments))
                .toArray()
            ;

            return this.method.invoke(this.target, arguments);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception)
                throw (Exception) e.getTargetException();

            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of initializer runner " + context.pipelineTag().pipeline() + "#" + context.componentTag().id()
                    + " has thrown an unexpected exception of type " + e.getTargetException().getClass().getName(), e.getTargetException()
            );
        }
        catch (IllegalAccessException e) {
            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of initializer runner " + context.pipelineTag().pipeline() + "#" + context.componentTag().id()
                    + " unexpectedly has illegal access", e
            );
        }
    }

    @Override
    public String defaultId()
    {
        return this.target.getClass().getName() + "#" + this.method.getName();
    }

    public Object target()
    {
        return this.target;
    }

    public void build(CompiledMethod<InitializerConfig, Object, I> compiled)
    {
        this.method = compiled.method();
        this.argumentMappers = compiled.mappers();
    }

    @Override
    public Object describe()
    {
        return this.target();
    }
}
