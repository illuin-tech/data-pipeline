package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.PipelineTag;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record ResultDescriptor<R extends Result>(
    String uid,
    PipelineTag tag,
    Instant createdAt,
    R payload
) implements Indexable {
    public ResultDescriptor(String uid, PipelineTag tag, R payload)
    {
        this(uid, tag, Instant.now(), payload);
    }
}
