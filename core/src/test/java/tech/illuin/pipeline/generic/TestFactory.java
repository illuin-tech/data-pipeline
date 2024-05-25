package tech.illuin.pipeline.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestFactory
{
    private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

    public static A initializer(Void input, Context context, UIDGenerator generator)
    {
        return new A(generator.generate(), List.of(
            new B(generator.generate(), "b1"),
            new B(generator.generate(), "b2")
        ));
    }

    public static A initializerOfEmpty(Void input, Context context, UIDGenerator generator)
    {
        return new A(generator.generate(), Collections.emptyList());
    }
}
