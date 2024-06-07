package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.ComponentTag;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record ResultDescriptor<R extends Result>(
    String uid,
    ComponentTag tag,
    Instant createdAt,
    R result
) implements Indexable {
    public ResultDescriptor(String uid, ComponentTag tag, R result)
    {
        this(uid, tag, Instant.now(), result);
    }
}
