package tech.illuin.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.output.Output;

public class PipelineHelperTest {

    @Test
    public void newContext() {
        Pipeline pipeline = Pipeline.ofSimple("my-test").build();
        Context<?> context = pipeline.newContext();
        assertTrue(context.parent().isEmpty());
    }

    @Test
    public void newContextWithParent() throws PipelineException {
        Pipeline pipeline = Pipeline.ofSimple("my-test").build();
        Output<A> output = pipeline.run();
        Context<A> context = pipeline.newContext(output);
        assertEquals(output, context.parent().get());
    }
}
