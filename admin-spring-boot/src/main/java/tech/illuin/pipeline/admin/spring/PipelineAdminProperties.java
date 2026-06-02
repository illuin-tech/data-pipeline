package tech.illuin.pipeline.admin.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "data-pipeline.admin")
public record PipelineAdminProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("/pipeline-admin") String path,
    List<String> include,
    List<String> exclude
) {
}
