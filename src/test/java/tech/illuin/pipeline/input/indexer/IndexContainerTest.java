package tech.illuin.pipeline.input.indexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.input.uid_generator.TSIDGenerator;

import java.util.Collections;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexContainerTest
{
    @Test
    public void testContainer()
    {
        var container = new IndexContainer();

        var b0 = new B(TSIDGenerator.INSTANCE.generate(), "0");
        var b1 = new B(TSIDGenerator.INSTANCE.generate(), "1");
        var b2 = new B(TSIDGenerator.INSTANCE.generate(), "2");
        var a0 = new A(TSIDGenerator.INSTANCE.generate(), Collections.emptyList());

        container.index(b0);
        container.index(b1);
        container.index(b2);
        container.index(a0);

        Assertions.assertEquals(4, container.stream().count());

        Assertions.assertTrue(container.contains(b0));
        Assertions.assertTrue(container.get(b0.uid()).isPresent());
        Assertions.assertSame(container.get(b0.uid()).orElse(null), b0);

        Assertions.assertTrue(container.contains(b1));
        Assertions.assertTrue(container.get(b1.uid()).isPresent());
        Assertions.assertSame(container.get(b1.uid()).orElse(null), b1);

        Assertions.assertTrue(container.contains(b2));
        Assertions.assertTrue(container.get(b2.uid()).isPresent());
        Assertions.assertSame(container.get(b2.uid()).orElse(null), b2);

        Assertions.assertTrue(container.contains(a0));
        Assertions.assertTrue(container.get(a0.uid()).isPresent());
        Assertions.assertTrue(container.get(a0.uid(), A.class).isPresent());
        Assertions.assertSame(container.get(a0.uid()).orElse(null), a0);
    }
}
