package tech.illuin.pipeline.generic.model;

import tech.illuin.pipeline.input.indexer.Indexable;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record A(
    String uid,
    List<B> bs
) implements Indexable {}
