package tech.illuin.pipeline.input.indexer;

import java.util.Collection;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public non-sealed interface MultiIndexer<T> extends Indexer<T>
{
    Collection<? extends Indexable> resolve(T payload);

    @Override
    default void index(T payload, IndexContainer container)
    {
        this.resolve(payload).forEach(container::index);
    }

    static <P extends Indexable> SingleIndexer<P> auto()
    {
        return p -> p;
    }
}
