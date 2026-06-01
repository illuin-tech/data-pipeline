package tech.illuin.pipeline.admin.micronaut;

import io.micrometer.core.instrument.Meter;
import io.micronaut.context.annotation.Factory;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Singleton;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.DefaultPipelineProvider;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;
import tech.illuin.pipeline.observer.descriptor.model.*;

import java.util.Collections;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Factory
@SerdeImport(PipelineDescription.class)
@SerdeImport(InitializerDescription.class)
@SerdeImport(StepDescription.class)
@SerdeImport(SinkDescription.class)
@SerdeImport(Metric.class)
@SerdeImport(Meter.Id.class)
public class PipelineAdminFactory
{
    @Singleton
    public PipelineAdminService pipelineAdminService(
        List<Pipeline<?>> pipelines,
        List<PipelineProvider> providers,
        PipelineAdminProperties properties
    ) {
        if (!properties.enabled())
            return null;

        PipelineProvider provider = new DefaultPipelineProvider(
            pipelines == null ? Collections.emptyList() : pipelines,
            providers == null ? Collections.emptyList() : providers,
            properties.include(),
            properties.exclude()
        );

        return new PipelineAdminService(provider);
    }

    @Singleton
    public PipelineAdminRenderer pipelineAdminRenderer(PipelineAdminProperties properties) {
        return new PipelineAdminRenderer(properties.path());
    }
}
