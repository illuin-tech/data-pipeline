package tech.illuin.pipeline.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;
import tech.illuin.pipeline.observer.descriptor.model.Metric;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdminService
{
    private final PipelineProvider provider;
    private final Cache<String, PipelineDescription> descriptionCache;

    private static final Duration DEFAULT_TTL = Duration.ofSeconds(10);

    public PipelineAdminService(PipelineProvider provider)
    {
        this(provider, DEFAULT_TTL);
    }

    public PipelineAdminService(PipelineProvider provider, Duration ttl)
    {
        this.provider = provider;
        this.descriptionCache = Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .build();
    }

    public List<PipelineDescription> listPipelines()
    {
        return this.provider.getPipelines().stream()
            .map(this::describe)
            .collect(Collectors.toList());
    }

    public Optional<PipelineDescription> getPipeline(String id)
    {
        return this.provider.getPipelines().stream()
            .filter(p -> p.id().equals(id))
            .findFirst()
            .map(this::describe);
    }

    public Map<String, Object> getGlobalKpis()
    {
        Collection<? extends Pipeline<?>> pipelines = this.provider.getPipelines();
        long count = pipelines.size();
        long totalRuns = 0;
        long totalSuccess = 0;

        for (Pipeline<?> pipeline : pipelines)
        {
            PipelineDescription desc = this.describe(pipeline);
            totalRuns += this.getMetricValue(desc, "pipeline.run.total");
            totalSuccess += this.getMetricValue(desc, "pipeline.run.success");
        }

        double successRate = totalRuns > 0 ? (double) totalSuccess / totalRuns : 0;
        return Map.of(
            "pipelineCount", count,
            "totalRuns", totalRuns,
            "successRate", successRate
        );
    }

    public Optional<Map<String, Object>> getPipelineKpis(String id)
    {
        return this.getPipeline(id).map(desc -> {
            long totalRuns = this.getMetricValue(desc, "pipeline.run.total");
            long totalSuccess = this.getMetricValue(desc, "pipeline.run.success");
            double successRate = totalRuns > 0 ? (double) totalSuccess / totalRuns : 0;

            return Map.of(
                "totalRuns", totalRuns,
                "successRate", successRate
            );
        });
    }

    private PipelineDescription describe(Pipeline<?> pipeline)
    {
        return this.descriptionCache.get(pipeline.id(), k -> pipeline.describe());
    }

    private long getMetricValue(PipelineDescription desc, String key)
    {
        if (desc == null || desc.metrics() == null)
            return 0;
        Metric metric = desc.metrics().get(key);
        if (metric == null || metric.values() == null)
            return 0;
        long sum = 0;
        for (Number val : metric.values().values())
            sum += val.longValue();
        return sum;
    }
}
