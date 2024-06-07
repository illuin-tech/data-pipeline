package tech.illuin.pipeline.input.indexer;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public sealed interface Indexer<T> permits SingleIndexer, MultiIndexer
{
    void index(T payload, IndexContainer container);
}
