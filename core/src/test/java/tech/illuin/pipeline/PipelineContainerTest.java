package tech.illuin.pipeline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.sink.SinkTest.createPipelineWithSinks_asyncException;
import static tech.illuin.pipeline.sink.SinkTest.createPipelineWithSinks_syncException;
import static tech.illuin.pipeline.step.AnnotationTest.createAnnotatedPipeline;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineContainerTest
{
    @Test
    public void testCreate()
    {
        var container = Assertions.assertDoesNotThrow(PipelineContainer::new);
        Assertions.assertEquals(0, container.count());
    }

    @Test
    public void testAddAndGet()
    {
        var container = Assertions.assertDoesNotThrow(PipelineContainer::new);

        var a = createAnnotatedPipeline();
        var b = createPipelineWithSinks_syncException(new AtomicInteger(0));
        var c = createPipelineWithSinks_asyncException(new AtomicInteger(0));

        container.register(a);
        container.register(List.of(b, c));

        Assertions.assertEquals(3, container.count());

        Assertions.assertSame(a, container.get(a.id()));
        Assertions.assertSame(b, container.get(b.id()));
        Assertions.assertSame(c, container.get(c.id()));

        container.register(a);

        Assertions.assertEquals(3, container.count());
    }
}
