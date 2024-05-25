package tech.illuin.pipeline.input.author_resolver;

import tech.illuin.pipeline.context.Context;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface AuthorResolver<T>
{
    String ANONYMOUS = "anonymous";

    String resolve(T input, Context context);

    static <T> String anonymous(T input, Context context)
    {
        return ANONYMOUS;
    }
}
