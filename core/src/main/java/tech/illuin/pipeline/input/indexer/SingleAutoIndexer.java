package tech.illuin.pipeline.input.indexer;

public class SingleAutoIndexer<T extends Indexable> implements SingleIndexer<T>
{
    @Override
    public Indexable resolve(T payload)
    {
        return payload;
    }
}
