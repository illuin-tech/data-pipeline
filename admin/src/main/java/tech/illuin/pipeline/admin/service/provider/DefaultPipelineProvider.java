package tech.illuin.pipeline.admin.service.provider;

import tech.illuin.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class DefaultPipelineProvider implements PipelineProvider
{
    private final List<Pipeline<?>> pipelines;
    private final List<PipelineProvider> providers;
    private final List<String> include;
    private final List<String> exclude;

    public DefaultPipelineProvider()
    {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public DefaultPipelineProvider(
        Collection<? extends Pipeline<?>> pipelines,
        Collection<? extends PipelineProvider> providers,
        Collection<String> include,
        Collection<String> exclude
    ) {
        this.pipelines = new ArrayList<>(pipelines);
        this.providers = new ArrayList<>(providers);
        this.include = new ArrayList<>(include);
        this.exclude = new ArrayList<>(exclude);
    }

    @Override
    public Collection<? extends Pipeline<?>> getPipelines()
    {
        List<Pipeline<?>> allPipelines = new ArrayList<>(this.pipelines);
        for (PipelineProvider provider : this.providers)
            allPipelines.addAll(provider.getPipelines());

        return allPipelines.stream()
            .filter(p -> this.include.isEmpty() || this.include.contains(p.id()))
            .filter(p -> !this.exclude.contains(p.id()))
            .sorted(Comparator.comparing(Pipeline::id))
            .collect(Collectors.toList());
    }
}
