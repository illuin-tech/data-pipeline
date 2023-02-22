package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public abstract class Result implements Indexable
{
    private final String uid;
    private final Instant createdAt;

    public Result()
    {
        this.createdAt = Instant.now();
        this.uid = RESULT_PREFIX + Indexable.generateUid(this.createdAt);
    }

    @Override
    public final String uid()
    {
        return this.uid;
    }

    public final Instant createdAt()
    {
        return this.createdAt;
    }

    public String type()
    {
        return this.getClass().getName();
    }
}
