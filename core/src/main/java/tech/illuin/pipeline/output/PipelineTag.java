package tech.illuin.pipeline.output;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record PipelineTag(
    String uid,
    String pipeline,
    String author
) implements Indexable {}
