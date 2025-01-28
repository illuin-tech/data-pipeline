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
public class InitializerBuilder<I> extends ComponentBuilder<Object, I, InitializerDescriptor<I>, InitializerConfig>
{
    private String id;
    private Initializer<I> initializer;
    private InitializerErrorHandler errorHandler;

    public InitializerBuilder()
    {
        super(InitializerConfig.class, new InitializerMethodArgumentResolver<>(), InitializerMethodValidators.validator);
    }

    @Override
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
    protected void fillDefaults()
    {
        if (this.id == null)
            this.id = this.initializer.defaultId();
        if (this.errorHandler == null)
            this.errorHandler = InitializerErrorHandler.RETHROW_ALL;
    }
    
    public InitializerBuilder<I> initializer(Initializer<I> initializer)
    {
        this.initializer = initializer;
        return this;
    }

    public InitializerBuilder<I> initializer(Object target)
    {
        this.initializer = Initializer.of(target);
        return this;
    }

    public InitializerBuilder<I> withId(String id)
    {
        this.id = id;
        return this;
    }

    public InitializerBuilder<I> withErrorHandler(InitializerErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    protected InitializerDescriptor<I> build()
    {
        if (this.initializer == null)
            throw new IllegalStateException("An InitializerDescriptor cannot be built from a null initializer");

        Optional<InitializerConfig> config;
        if (this.initializer instanceof InitializerRunner<I> initializerRunner)
        {
            CompiledMethod<InitializerConfig, Object, I> compiled = this.compiler.compile(initializerRunner.target());
            initializerRunner.build(compiled);
            config = Optional.of(compiled.config());
        }
        else
            config = this.checkAnnotation(this.initializer);

        config.ifPresent(this::fillFromAnnotation);

        this.fillDefaults();
        this.validate();

        return new InitializerDescriptor<>(
            this.id,
            this.initializer,
            this.errorHandler
        );
    }

    private void validate()
    {
        if (this.id == null)
            throw new IllegalStateException("An initializer cannot have a null id, make sure the defaultId() return value is not null");
    }
}
