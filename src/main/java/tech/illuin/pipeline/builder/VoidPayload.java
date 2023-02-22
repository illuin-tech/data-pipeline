package tech.illuin.pipeline.builder;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class VoidPayload implements Indexable
{
    private final String uid;

    VoidPayload()
    {
        this.uid = Indexable.generateUid();
    }

    @Override
    public String uid() { return this.uid; }
}
