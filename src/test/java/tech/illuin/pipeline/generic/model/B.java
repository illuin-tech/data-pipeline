package tech.illuin.pipeline.generic.model;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record B(
    String uid,
    String name
) implements Indexable {}
