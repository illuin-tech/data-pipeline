package tech.illuin.pipeline.input.indexer;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public non-sealed interface SingleIndexer<T> extends Indexer<T>
{
    Indexable resolve(T payload);

    @Override
    default void index(T payload, IndexContainer container)
    {
        container.index(this.resolve(payload));
    }

    static <P extends Indexable> SingleIndexer<P> auto()
    {
        return p -> p;
    }
}
