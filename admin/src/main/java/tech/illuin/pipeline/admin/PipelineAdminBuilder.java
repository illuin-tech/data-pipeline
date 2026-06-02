package tech.illuin.pipeline.admin;

import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.provider.DefaultPipelineProvider;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdminBuilder
{
    private final List<Pipeline<?>> pipelines = new ArrayList<>();
    private final List<PipelineProvider> pipelineProviders = new ArrayList<>();
    private final List<String> include = new ArrayList<>();
    private final List<String> exclude = new ArrayList<>();
    private int port = 8080;
    private String host = "0.0.0.0";

    public PipelineAdminBuilder addPipeline(Pipeline<?> pipeline)
    {
        this.pipelines.add(pipeline);
        return this;
    }

    public PipelineAdminBuilder addPipeline(Collection<Pipeline<?>> pipelines)
    {
        this.pipelines.addAll(pipelines);
        return this;
    }

    public PipelineAdminBuilder addPipeline(Pipeline<?>... pipelines)
    {
        Collections.addAll(this.pipelines, pipelines);
        return this;
    }

    public PipelineAdminBuilder addPipelineProvider(PipelineProvider provider)
    {
        this.pipelineProviders.add(provider);
        return this;
    }

    public PipelineAdminBuilder addPipelineProvider(Collection<PipelineProvider> providers)
    {
        this.pipelineProviders.addAll(providers);
        return this;
    }

    public PipelineAdminBuilder addPipelineProvider(PipelineProvider... providers)
    {
        Collections.addAll(this.pipelineProviders, providers);
        return this;
    }

    public PipelineAdminBuilder include(String id)
    {
        this.include.add(id);
        return this;
    }

    public PipelineAdminBuilder include(String... ids)
    {
        Collections.addAll(this.include, ids);
        return this;
    }

    public PipelineAdminBuilder exclude(String id)
    {
        this.exclude.add(id);
        return this;
    }

    public PipelineAdminBuilder exclude(String... ids)
    {
        Collections.addAll(this.exclude, ids);
        return this;
    }

    public PipelineAdminBuilder setPort(int port)
    {
        this.port = port;
        return this;
    }

    public PipelineAdminBuilder setHost(String host)
    {
        this.host = host;
        return this;
    }

    public PipelineAdmin build()
    {
        PipelineProvider provider = new DefaultPipelineProvider(
            this.pipelines,
            this.pipelineProviders,
            this.include,
            this.exclude
        );
        return new PipelineAdmin(provider, this.host, this.port);
    }
}
