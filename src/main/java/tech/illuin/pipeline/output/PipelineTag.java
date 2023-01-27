package tech.illuin.pipeline.output;

import java.util.UUID;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record PipelineTag(
    UUID uid,
    String pipeline
) {}
