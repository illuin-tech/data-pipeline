package tech.illuin.pipeline.sink.builder;

import tech.illuin.pipeline.builder.ComponentBuilder;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.annotation.SinkConfig;
import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;
import tech.illuin.pipeline.sink.execution.wrapper.SinkWrapper;
import tech.illuin.pipeline.sink.runner.SinkRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkBuilder<P> extends ComponentBuilder<Object, Object, P, SinkDescriptor<P>, SinkConfig>
{
    private String id;
    private Sink<P> sink;
    private SinkWrapper<P> executionWrapper;
    private SinkErrorHandler errorHandler;
    private Boolean async;

    public SinkBuilder()
    {
        super(SinkConfig.class, new SinkMethodArgumentResolver<>(), SinkMethodValidators.validators);
    }

    @Override
    protected void fillFromAnnotation(SinkConfig annotation)
    {
        try {
            if (this.executionWrapper == null && annotation.id() != null && !annotation.id().isBlank())
                this.id = annotation.id();
            if (this.async == null)
                this.async = annotation.async();
            if (this.errorHandler == null && annotation.errorHandler() != null && annotation.errorHandler() != SinkErrorHandler.class)
                this.errorHandler = annotation.errorHandler().getConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while attempting to instantiate a SinkConfig argument", e);
        }
    }

    @Override
    protected void fillDefaults()
    {
        if (this.id == null)
            this.id = this.sink.defaultId();
        if (this.async == null)
            this.async = false;
        if (this.executionWrapper == null)
            this.executionWrapper = SinkWrapper::noOp;
        if (this.errorHandler == null)
            this.errorHandler = SinkErrorHandler.RETHROW_ALL;
    }
    
    public SinkBuilder<P> sink(Sink<P> sink)
    {
        this.sink = sink;
        return this;
    }

    public SinkBuilder<P> sink(Object target)
    {
        this.sink = Sink.of(target);
        return this;
    }

    public SinkBuilder<P> withId(String id)
    {
        this.id = id;
        return this;
    }

    public SinkBuilder<P> withWrapper(SinkWrapper<P> wrapper)
    {
        this.executionWrapper = wrapper;
        return this;
    }

    public SinkBuilder<P> withErrorHandler(SinkErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    public SinkBuilder<P> setAsync(boolean async)
    {
        this.async = async;
        return this;
    }

    @Override
    protected SinkDescriptor<P> build()
    {
        if (this.sink == null)
            throw new IllegalStateException("A SinkDescriptor cannot be built from a null sink");

        Optional<SinkConfig> config;
        if (this.sink instanceof SinkRunner<P> sinkRunner)
        {
            CompiledMethod<SinkConfig, Object, Object, P> compiled = this.compiler.compile(sinkRunner.target());
            sinkRunner.build(compiled);
            config = Optional.of(compiled.config());
        }
        else
            config = this.checkAnnotation(this.sink);

        config.ifPresent(this::fillFromAnnotation);

        this.fillDefaults();

        return new SinkDescriptor<>(
            this.id,
            this.sink,
            this.async,
            this.executionWrapper,
            this.errorHandler
        );
    }
}
