package tech.illuin.pipeline.generic.pipeline.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.pipeline.sink.execution.error.WrapAll;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.Sink;
import tech.illuin.pipeline.sink.annotation.SinkConfig;

import java.util.function.Consumer;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestAnnotatedSinks
{
    @SinkConfig(id = "test-sink-error-handler", errorHandler = WrapAll.class)
    public static class ErrorHandler<T extends Indexable> extends AnnotatedSink<T>
    {
        public ErrorHandler(String name, Consumer<T> function) { super(name, function); }
        public ErrorHandler(String name) { super(name); }
    }

    @SinkConfig(id = "test-sink-async", async = true)
    public static class Async<T extends Indexable> extends AnnotatedSink<T>
    {
        public Async(String name, Consumer<T> function) { super(name, function); }
        public Async(String name) { super(name); }
    }

    private abstract static class AnnotatedSink<P> implements Sink<P>
    {
        private final String name;
        private final Consumer<P> function;

        private static final Logger logger = LoggerFactory.getLogger(AnnotatedSink.class);

        public AnnotatedSink(String name, Consumer<P> function)
        {
            this.name = name;
            this.function = function;
        }

        public AnnotatedSink(String name)
        {
            this(name, in -> {});
        }

        @Override
        public void execute(Output<P> output, Context<P> context)
        {
            logger.info("test:{}: ~", this.name);
            this.function.accept(output.payload());
        }
    }
}
