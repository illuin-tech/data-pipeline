package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record ResultDescriptor<R extends Result>(
    String uid,
    Instant createdAt,
    R payload
) implements Indexable {
    public ResultDescriptor(String uid, R payload)
    {
        this(uid, Instant.now(), payload);
    }
}
