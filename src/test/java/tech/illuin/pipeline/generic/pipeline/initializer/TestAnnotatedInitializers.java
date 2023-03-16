package tech.illuin.pipeline.generic.pipeline.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.initializer.execution.error.WrapAll;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.Initializer;
import tech.illuin.pipeline.input.initializer.annotation.InitializerConfig;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestAnnotatedInitializers
{
    @InitializerConfig(id = "test-init-error-handler", errorHandler = WrapAll.class)
    public static class ErrorHandler<I, P extends Indexable> extends AnnotatedInitializer<I, P>
    {
        public ErrorHandler(String name, Function<I, P> function) { super(name, function); }
    }

    private abstract static class AnnotatedInitializer<I, P> implements Initializer<I, P>
    {
        private final String name;
        private final Function<I, P> function;

        private static final Logger logger = LoggerFactory.getLogger(AnnotatedInitializer.class);

        public AnnotatedInitializer(String name, Function<I, P> function)
        {
            this.name = name;
            this.function = function;
        }

        @Override
        public P initialize(I input, Context<P> context)
        {
            logger.info("test:{}: ~", this.name);
            return this.function.apply(input);
        }
    }
}
