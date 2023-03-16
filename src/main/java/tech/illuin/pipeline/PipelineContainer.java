package tech.illuin.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class PipelineContainer
{
    private final Map<String, Pipeline<?, ?>> pipelines;

    public PipelineContainer()
    {
        this.pipelines = new HashMap<>();
    }

    public Pipeline<?, ?> get(String id)
    {
        return this.pipelines.get(id);
    }

    public boolean has(String id)
    {
        return this.pipelines.containsKey(id);
    }

    public PipelineContainer register(Pipeline<?, ?> pipeline)
    {
        this.pipelines.put(pipeline.id(), pipeline);
        return this;
    }

    public PipelineContainer register(Collection<Pipeline<?, ?>> pipelines)
    {
        for (Pipeline<?, ?> pipeline : pipelines)
            this.register(pipeline);
        return this;
    }

    public int count()
    {
        return this.pipelines.size();
    }
}
