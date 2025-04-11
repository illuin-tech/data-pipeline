package tech.illuin.pipeline.step.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.MultiResult;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepRunner<T extends Indexable, I> implements Step<T, I>, Describable
{
    private final java.lang.Object target;
    private Method method;
    private List<MethodArgumentMapper<T, I>> argumentMappers;

    private static final Logger logger = LoggerFactory.getLogger(StepRunner.class);

    public StepRunner(java.lang.Object target)
    {
        if (target == null)
            throw new IllegalArgumentException("The target of a StepRunner cannot be null");
        this.target = target;
    }

    @Override
    public Result execute(T object, I input, Object payload, ResultView results, LocalContext context) throws Exception
    {
        logger.trace("{}#{} launching step over target {}#{}", context.pipelineTag().pipeline(), context.pipelineTag().uid(), this.target.getClass().getName(), Reflection.getMethodSignature(this.method));

        try {
            MethodArguments<T, I> originalArguments = new MethodArguments<>(
                object,
                input,
                payload,
                null,
                results,
                results,
                context,
                context.pipelineTag(),
                context.componentTag(),
                context.uidGenerator(),
                context.markerManager()
            );
            java.lang.Object[] arguments = this.argumentMappers.stream()
                .map(mapper -> mapper.map(originalArguments))
                .toArray()
            ;

            Object result = this.method.invoke(this.target, arguments);

            /* Valid cases should have corresponding MethodValidators */
            if (result instanceof Result)
                return (Result) result;
            if (result instanceof Collection<?> cResult)
                //noinspection unchecked
                return MultiResult.of((Collection<? extends Result>) cResult);

            if (result == null)
                throw new StepRunnerException("Step method returned a null result");
            throw new StepRunnerException("Step method returned an unexpected result type " + result.getClass().getName());
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception)
                throw (Exception) e.getTargetException();

            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of step runner " + context.pipelineTag().pipeline() + "#" + context.componentTag().id()
                    + " has thrown an unexpected exception of type " + e.getTargetException().getClass().getName(),
                e.getTargetException()
            );
        }
        catch (IllegalAccessException e) {
            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of step runner " + context.pipelineTag().pipeline() + "#" + context.componentTag().id()
                    + " unexpectedly has illegal access",
                e
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

    public void build(CompiledMethod<StepConfig, T, I> compiled)
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
