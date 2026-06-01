package tech.illuin.pipeline.admin.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.DefaultPipelineProvider;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;
import tech.illuin.pipeline.admin.spring.controller.PipelineAdminController;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@AutoConfiguration
@Import(PipelineAdminController.class)
@EnableConfigurationProperties(PipelineAdminProperties.class)
@ConditionalOnProperty(prefix = "data-pipeline.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PipelineAdminAutoConfiguration
{
    @Bean @ConditionalOnMissingBean
    public PipelineAdminService pipelineAdminService(
        @Autowired(required = false) List<Pipeline<?>> pipelines,
        @Autowired(required = false) List<PipelineProvider> providers,
        PipelineAdminProperties properties
    ) {
        PipelineProvider provider = new DefaultPipelineProvider(
            pipelines == null ? emptyList() : pipelines,
            providers == null ? emptyList() : providers,
            properties.include() == null ? emptyList() : properties.include(),
            properties.exclude() == null ? emptyList() : properties.exclude()
        );

        return new PipelineAdminService(provider);
    }

    @Bean @ConditionalOnMissingBean
    public PipelineAdminRenderer pipelineAdminRenderer(PipelineAdminProperties properties) {
        return new PipelineAdminRenderer(properties.path());
    }
}
