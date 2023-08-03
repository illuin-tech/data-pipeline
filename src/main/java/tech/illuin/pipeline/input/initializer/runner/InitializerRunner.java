package tech.illuin.pipeline.input.initializer.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory.MethodArgumentMapper;
import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;
import tech.illuin.pipeline.commons.Reflection;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerRunner<I, P> implements Initializer<I, P>
{
    private final Object target;
    private Method method;
    private List<MethodArgumentMapper<Object, I, P>> argumentMappers;

    private static final Logger logger = LoggerFactory.getLogger(InitializerRunner.class);

    public InitializerRunner(Object target)
    {
        if (target == null)
            throw new IllegalArgumentException("The target of an InitializerRunner cannot be null");
        this.target = target;
    }

    @Override
    public P initialize(I input, Context<P> context, UIDGenerator generator) throws Exception
    {
        if (!(context instanceof LocalContext<P> localContext))
            throw new IllegalArgumentException("Invalid context provided to an InitializerRunner instance");

        logger.trace("{}#{} launching initializer over target {}#{}", localContext.pipelineTag().pipeline(), localContext.pipelineTag().uid(), this.target.getClass().getName(), Reflection.getMethodSignature(this.method));

        MethodArguments<Object, I, P> originalArguments = new MethodArguments<>(
            null,
            input,
            null,
            null,
            null,
            null,
            localContext,
            localContext.pipelineTag(),
            localContext.componentTag(),
            generator
        );
        Object[] arguments = this.argumentMappers.stream()
            .map(mapper -> mapper.map(originalArguments))
            .toArray()
        ;

        //noinspection unchecked
        return (P) this.method.invoke(this.target, arguments);
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

    public void build(CompiledMethod<InitializerConfig, Object, I, P> compiled)
    {
        this.method = compiled.method();
        this.argumentMappers = compiled.mappers();
    }
}
