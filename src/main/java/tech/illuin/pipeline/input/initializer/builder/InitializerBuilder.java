package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.builder.ComponentBuilder;
import tech.illuin.pipeline.builder.runner_compiler.CompiledMethod;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;
import tech.illuin.pipeline.input.initializer.runner.InitializerRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerBuilder<I, P> extends ComponentBuilder<Object, I, P, InitializerDescriptor<I, P>, InitializerConfig>
{
    private String id;
    private Initializer<I, P> initializer;
    private InitializerErrorHandler<P> errorHandler;

    public InitializerBuilder()
    {
        super(InitializerConfig.class, new InitializerMethodArgumentResolver<>(), InitializerMethodValidators.validators);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void fillFromAnnotation(InitializerConfig annotation)
    {
        try {
            if (this.id == null && annotation.id() != null && !annotation.id().isBlank())
                this.id = annotation.id();
            if (this.errorHandler == null && annotation.errorHandler() != null && annotation.errorHandler() != InitializerErrorHandler.class)
                this.errorHandler = annotation.errorHandler().getConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while attempting to instantiate a SinkConfig argument", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void fillDefaults()
    {
        if (this.id == null)
            this.id = this.initializer.defaultId();
        if (this.errorHandler == null)
            this.errorHandler = (InitializerErrorHandler<P>) InitializerErrorHandler.RETHROW_ALL;
    }
    
    public InitializerBuilder<I, P> initializer(Initializer<I, P> initializer)
    {
        this.initializer = initializer;
        return this;
    }

    public InitializerBuilder<I, P> initializer(Object target)
    {
        this.initializer = Initializer.of(target);
        return this;
    }

    public InitializerBuilder<I, P> withId(String id)
    {
        this.id = id;
        return this;
    }

    public InitializerBuilder<I, P> withErrorHandler(InitializerErrorHandler<P> errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    protected InitializerDescriptor<I, P> build()
    {
        if (this.initializer == null)
            throw new IllegalStateException("An InitializerDescriptor cannot be built from a null initializer");

        Optional<InitializerConfig> config;
        if (this.initializer instanceof InitializerRunner<I, P> initializerRunner)
        {
            CompiledMethod<InitializerConfig, Object, I, P> compiled = this.compiler.compile(initializerRunner.target());
            initializerRunner.build(compiled);
            config = Optional.of(compiled.config());
        }
        else
            config = this.checkAnnotation(this.initializer);

        config.ifPresent(this::fillFromAnnotation);

        this.fillDefaults();

        return new InitializerDescriptor<>(
            this.id,
            this.initializer,
            this.errorHandler
        );
    }
}
