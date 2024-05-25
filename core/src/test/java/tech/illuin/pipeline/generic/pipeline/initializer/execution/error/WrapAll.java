package tech.illuin.pipeline.generic.pipeline.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

import java.util.Collections;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class WrapAll implements InitializerErrorHandler
{
    @Override
    public A handle(Exception exception, Context context, UIDGenerator generator)
    {
        return new A(generator.generate(), Collections.emptyList());
    }
}
