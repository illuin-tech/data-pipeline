package tech.illuin.pipeline.admin.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import java.util.List;

import static java.util.Collections.emptyList;

@ConfigurationProperties("data-pipeline.admin")
public record PipelineAdminProperties(
    @Bindable(defaultValue = "true") boolean enabled,
    @Bindable(defaultValue = "/pipeline-admin") String path,
    List<String> include,
    List<String> exclude
) {
    public PipelineAdminProperties(
        @Bindable(defaultValue = "true") boolean enabled,
        @Bindable(defaultValue = "/pipeline-admin") String path,
        @io.micronaut.core.annotation.Nullable List<String> include,
        @io.micronaut.core.annotation.Nullable List<String> exclude
    ) {
        this.enabled = enabled;
        this.path = path != null ? path : "/pipeline-admin";
        this.include = include != null ? include : emptyList();
        this.exclude = exclude != null ? exclude : emptyList();
    }
}
