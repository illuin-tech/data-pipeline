package tech.illuin.pipeline.admin.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.DefaultPipelineProvider;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@ApplicationScoped
public class PipelineAdminProducer
{
    @Produces @Singleton
    public static PipelineAdminService pipelineAdminService(Instance<Pipeline> pipelines, Instance<PipelineProvider> providers, PipelineAdminProperties properties)
    {
        if (!properties.enabled())
            return null;

        List<Pipeline<?>> pipelineList = new ArrayList<>();
        pipelines.forEach(pipelineList::add);

        List<PipelineProvider> providerList = new ArrayList<>();
        providers.forEach(providerList::add);

        PipelineProvider provider = new DefaultPipelineProvider(
            pipelineList,
            providerList,
            properties.include().orElse(Collections.emptyList()),
            properties.exclude().orElse(Collections.emptyList())
        );

        return new PipelineAdminService(provider);
    }

    @Produces @Singleton
    public static PipelineAdminRenderer pipelineAdminRenderer()
    {
        return new PipelineAdminRenderer("/pipeline-admin");
    }
}
