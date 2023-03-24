package tech.illuin.pipeline.generic.pipeline.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.initializer.execution.error.WrapAll;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.util.function.BiFunction;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestAnnotatedInitializers
{
    @InitializerConfig(id = "test-init")
    public static class Default<I, P extends Indexable> extends AnnotatedInitializer<I, P>
    {
        public Default(String name, BiFunction<UIDGenerator, I, P> function) { super(name, function); }
    }

    @InitializerConfig(id = "test-init-error-handler", errorHandler = WrapAll.class)
    public static class ErrorHandler<I, P extends Indexable> extends AnnotatedInitializer<I, P>
    {
        public ErrorHandler(String name, BiFunction<UIDGenerator, I, P> function) { super(name, function); }
    }

    private abstract static class AnnotatedInitializer<I, P> implements Initializer<I, P>
    {
        private final String name;
        private final BiFunction<UIDGenerator, I, P> function;

        private static final Logger logger = LoggerFactory.getLogger(AnnotatedInitializer.class);

        public AnnotatedInitializer(String name, BiFunction<UIDGenerator, I, P> function)
        {
            this.name = name;
            this.function = function;
        }

        @Override
        public P initialize(I input, Context<P> context, UIDGenerator generator)
        {
            logger.info("test:{}: ~", this.name);
            return this.function.apply(generator, input);
        }
    }
}
