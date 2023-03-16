package tech.illuin.pipeline.generic.pipeline.initializer.execution.error;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;

import java.util.Collections;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class WrapAll implements InitializerErrorHandler<A>
{
    @Override
    public A handle(Exception exception, Context<A> context)
    {
        return new A(Indexable.generateUid(), Collections.emptyList());
    }
}
