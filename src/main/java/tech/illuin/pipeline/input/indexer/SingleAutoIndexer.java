package tech.illuin.pipeline.input.indexer;

public class SingleAutoIndexer<T> implements SingleIndexer<T>
{
    @Override
    public Indexable resolve(T payload)
    {
        if (payload instanceof Indexable indexable)
            return indexable;
        throw new IllegalArgumentException("The provided payload of type " + payload.getClass().getName() + " is not Indexable");
    }
}
