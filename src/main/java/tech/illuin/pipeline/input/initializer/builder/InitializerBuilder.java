package tech.illuin.pipeline.input.initializer.builder;

import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class InitializerBuilder<I, P>
{
    private String id;
    private Initializer<I, P> initializer;
    private InitializerErrorHandler<P> errorHandler;

    @SuppressWarnings("unchecked")
    private void checkAnnotation()
    {
        try {
            var type = this.initializer.getClass();
            if (!type.isAnnotationPresent(InitializerConfig.class))
                return;

            var annotation = type.getAnnotation(InitializerConfig.class);

            if (annotation.id() != null && !annotation.id().isBlank())
                this.id = annotation.id();
            if (this.errorHandler == null && annotation.errorHandler() != null && annotation.errorHandler() != InitializerErrorHandler.class)
                this.errorHandler = annotation.errorHandler().getConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while attempting to instantiate a SinkConfig argument", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void fillDefaults()
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

    InitializerDescriptor<I, P> build()
    {
        if (this.initializer == null)
            throw new IllegalStateException("An InitializerDescriptor cannot be built from a null initializer");

        this.checkAnnotation();
        this.fillDefaults();

        return new InitializerDescriptor<>(
            this.id,
            this.initializer,
            this.errorHandler
        );
    }
}
