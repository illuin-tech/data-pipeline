package tech.illuin.pipeline.admin.service;

import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;
import tech.illuin.pipeline.observer.descriptor.model.Metric;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdminService
{
    private final PipelineProvider provider;

    public PipelineAdminService(PipelineProvider provider)
    {
        this.provider = provider;
    }

    public List<PipelineDescription> listPipelines()
    {
        return this.provider.getPipelines().stream()
            .map(Pipeline::describe)
            .collect(Collectors.toList());
    }

    public Optional<PipelineDescription> getPipeline(String id)
    {
        return this.provider.getPipelines().stream()
            .filter(p -> p.id().equals(id))
            .findFirst()
            .map(Pipeline::describe);
    }

    public Map<String, Object> getGlobalKpis()
    {
        Collection<? extends Pipeline<?>> pipelines = this.provider.getPipelines();
        long count = pipelines.size();
        long totalRuns = 0;
        long totalSuccess = 0;

        for (Pipeline<?> pipeline : pipelines)
        {
            PipelineDescription desc = pipeline.describe();
            totalRuns += this.getMetricValue(desc, "pipeline.run.total");
            totalSuccess += this.getMetricValue(desc, "pipeline.run.success");
        }

        double successRate = totalRuns > 0 ? (double) totalSuccess / totalRuns : 0;

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("pipelineCount", count);
        kpis.put("totalRuns", totalRuns);
        kpis.put("successRate", successRate);

        return kpis;
    }

    public Optional<Map<String, Object>> getPipelineKpis(String id)
    {
        return this.getPipeline(id).map(desc -> {
            long totalRuns = this.getMetricValue(desc, "pipeline.run.total");
            long totalSuccess = this.getMetricValue(desc, "pipeline.run.success");
            double successRate = totalRuns > 0 ? (double) totalSuccess / totalRuns : 0;

            Map<String, Object> kpis = new HashMap<>();
            kpis.put("totalRuns", totalRuns);
            kpis.put("successRate", successRate);
            return kpis;
        });
    }

    private long getMetricValue(PipelineDescription desc, String key)
    {
        if (desc.metrics() == null)
            return 0;
        Metric metric = desc.metrics().get(key);
        if (metric == null || metric.values() == null)
            return 0;
        return metric.values().values().stream().mapToLong(Number::longValue).sum();
    }
}
