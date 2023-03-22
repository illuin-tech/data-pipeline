package tech.illuin.pipeline.builder;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record VoidPayload(
    String uid
) implements Indexable {}
