package tech.illuin.pipeline.step.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.step.Step;
import tech.illuin.pipeline.step.annotation.StepConfig;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepRunner<T extends Indexable, I> implements Step<T, I>
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
    public Result execute(T object, I input, Object payload, ResultView results, Context context) throws Exception
    {
        if (!(context instanceof LocalContext localContext))
            throw new IllegalArgumentException("Invalid context provided to a SinkRunner instance");

        logger.trace("{}#{} launching step over target {}#{}", localContext.pipelineTag().pipeline(), localContext.pipelineTag().uid(), this.target.getClass().getName(), Reflection.getMethodSignature(this.method));

        try {
            MethodArguments<T, I> originalArguments = new MethodArguments<>(
                object,
                input,
                payload,
                null,
                results,
                results,
                context,
                localContext.pipelineTag(),
                localContext.componentTag(),
                localContext.uidGenerator(),
                localContext.logMarker()
            );
            java.lang.Object[] arguments = this.argumentMappers.stream()
                .map(mapper -> mapper.map(originalArguments))
                .toArray()
            ;
            return (Result) this.method.invoke(this.target, arguments);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception)
                throw (Exception) e.getTargetException();

            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of step runner " + localContext.pipelineTag().pipeline() + "#" + localContext.componentTag().id()
                    + " has thrown an unexpected exception of type " + e.getTargetException().getClass().getName(),
                e.getTargetException()
            );
        }
        catch (IllegalAccessException e) {
            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of step runner " + localContext.pipelineTag().pipeline() + "#" + localContext.componentTag().id()
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
}
