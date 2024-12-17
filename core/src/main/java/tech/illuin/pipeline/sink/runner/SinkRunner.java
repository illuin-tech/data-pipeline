package tech.illuin.pipeline.sink.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.observer.descriptor.describable.Describable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.step.runner.StepRunnerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkRunner implements Sink, Describable
{
    private final Object target;
    private Method method;
    private List<MethodArgumentMapper<Object, Object>> argumentMappers;

    private static final Logger logger = LoggerFactory.getLogger(SinkRunner.class);

    public SinkRunner(Object target)
    {
        if (target == null)
            throw new IllegalArgumentException("The target of a SinkRunner cannot be null");
        this.target = target;
    }

    @Override
    public void execute(Output output, Context context) throws Exception
    {
        if (!(context instanceof LocalContext localContext))
            throw new IllegalArgumentException("Invalid context provided to a SinkRunner instance");

        logger.trace("{}#{} launching sink over target {}#{}", localContext.pipelineTag().pipeline(), localContext.pipelineTag().uid(), this.target.getClass().getName(), Reflection.getMethodSignature(this.method));

        try {
            MethodArguments<Object, Object> originalArguments = new MethodArguments<>(
                null,
                localContext.input(),
                output.payload(),
                output,
                null,
                output.results(),
                localContext,
                localContext.pipelineTag(),
                localContext.componentTag(),
                localContext.uidGenerator(),
                localContext.logMarker()
            );
            Object[] arguments = this.argumentMappers.stream()
                .map(mapper -> mapper.map(originalArguments))
                .toArray()
            ;
            this.method.invoke(this.target, arguments);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception)
                throw (Exception) e.getTargetException();

            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of sink runner " + localContext.pipelineTag().pipeline() + "#" + localContext.componentTag().id()
                    + " has thrown an unexpected exception of type " + e.getTargetException().getClass().getName(), e.getTargetException()
            );
        }
        catch (IllegalAccessException e) {
            throw new StepRunnerException(
                "The target method " + this.method.getName() + " of sink runner " + localContext.pipelineTag().pipeline() + "#" + localContext.componentTag().id()
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

    public void build(CompiledMethod<SinkConfig, Object, Object> compiled)
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
