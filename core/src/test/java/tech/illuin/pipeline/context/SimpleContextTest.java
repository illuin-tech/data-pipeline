package tech.illuin.pipeline.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SimpleContextTest
{
    @Test
    public void copyFrom()
    {
        Context contextA = new SimpleContext().set("a", true).set("b", false);
        Context contextB = new SimpleContext();

        contextB.copyFrom(contextA);

        Assertions.assertEquals(true, contextB.get("a").orElse(null));
        Assertions.assertEquals(false, contextB.get("b").orElse(null));
    }

    @Test
    public void copyFrom__shouldCombine()
    {
        Context contextA = new SimpleContext().set("a", true).set("b", false);
        Context contextB = new SimpleContext().set("c", "abcde").set("d", 123);

        contextB.copyFrom(contextA);

        Assertions.assertEquals(true, contextB.get("a").orElse(null));
        Assertions.assertEquals(false, contextB.get("b").orElse(null));
        Assertions.assertEquals("abcde", contextB.get("c").orElse(null));
        Assertions.assertEquals(123, contextB.get("d").orElse(null));
    }

    @Test
    public void copyFrom__shouldOverwrite()
    {
        Context contextA = new SimpleContext().set("a", true).set("b", true);
        Context contextB = new SimpleContext().set("a", false);

        contextB.copyFrom(contextA);

        Assertions.assertEquals(true, contextB.get("a").orElse(null));
        Assertions.assertEquals(true, contextB.get("b").orElse(null));
    }
}
