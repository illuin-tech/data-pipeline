package tech.illuin.pipeline.output;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record ComponentTag(
    String uid,
    PipelineTag pipelineTag,
    String id,
    ComponentFamily family
) implements Indexable {}
