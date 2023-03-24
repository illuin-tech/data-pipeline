package tech.illuin.pipeline.output.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.generic.pipeline.initializer.TestAnnotatedInitializers.Default;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class OutputFactoryTest
{
    @Test
    public void testPipeline()
    {
        AtomicInteger counter = new AtomicInteger(0);

        var pipeline = Assertions.assertDoesNotThrow(() -> Pipeline.ofPayload("test-output-factory", new Default<Void, B>("b-init", (gen, v) -> new B(gen.generate(), "b")))
            .setOutputFactory(CustomOutput::new)
            .registerSink((out, ctx) -> {
                if (out instanceof CustomOutput)
                    counter.incrementAndGet();
            })
            .build()
        );

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    private static class CustomOutput extends Output<B>
    {
        public CustomOutput(PipelineTag tag, Void input, Context<B> context)
        {
            super(tag);
        }
    }
}
