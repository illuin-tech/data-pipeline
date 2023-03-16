package tech.illuin.pipeline.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;

import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestFactory
{
    private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

    public static A initializer(Void input, Context<A> context)
    {
        return new A(List.of(
            new B("b1"),
            new B("b2")
        ));
    }

    public static A initializerOfEmpty(Void input, Context<A> context)
    {
        return new A(Collections.emptyList());
    }
}
