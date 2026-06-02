package tech.illuin.pipeline.admin.service.provider;

import tech.illuin.pipeline.Pipeline;

import java.util.Collection;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface PipelineProvider
{
    Collection<? extends Pipeline<?>> getPipelines();
}
