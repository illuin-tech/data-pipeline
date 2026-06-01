package tech.illuin.pipeline.admin.quarkus;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "data-pipeline.admin")
public interface PipelineAdminProperties
{
    @WithDefault("true") boolean enabled();
    Optional<List<String>> include();
    Optional<List<String>> exclude();
}
