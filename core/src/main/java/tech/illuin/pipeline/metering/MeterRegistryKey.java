package tech.illuin.pipeline.metering;

import io.micrometer.core.instrument.Tag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public enum MeterRegistryKey
{
    /* Pipeline Metrics */
    PIPELINE_RUN_KEY("pipeline.run"),
    PIPELINE_RUN_TOTAL_KEY("pipeline.run.total"),
    PIPELINE_RUN_SUCCESS_KEY("pipeline.run.success"),
    PIPELINE_RUN_FAILURE_KEY("pipeline.run.failure"),
    PIPELINE_RUN_ERROR_TOTAL_KEY("pipeline.run.error.total"),
    /* Initialization Metrics */
    PIPELINE_INITIALIZATION_RUN_KEY("pipeline.initialization.run"),
    PIPELINE_INITIALIZATION_RUN_TOTAL_KEY("pipeline.initialization.run.total"),
    PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY("pipeline.initialization.run.success"),
    PIPELINE_INITIALIZATION_RUN_FAILURE_KEY("pipeline.initialization.run.failure"),
    PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY("pipeline.initialization.error.total"),
    /* Step Metrics */
    PIPELINE_STEP_RUN_KEY("pipeline.step.run"),
    PIPELINE_STEP_RUN_TOTAL_KEY("pipeline.step.run.total"),
    PIPELINE_STEP_RUN_SUCCESS_KEY("pipeline.step.run.success"),
    PIPELINE_STEP_RUN_FAILURE_KEY("pipeline.step.run.failure"),
    PIPELINE_STEP_RESULT_TOTAL_KEY("pipeline.step.result.total"),
    PIPELINE_STEP_ERROR_TOTAL_KEY("pipeline.step.error.total"),
    /* Sink Metrics */
    PIPELINE_SINK_RUN_KEY("pipeline.sink.run"),
    PIPELINE_SINK_RUN_TOTAL_KEY("pipeline.sink.run.total"),
    PIPELINE_SINK_RUN_SUCCESS_KEY("pipeline.sink.run.success"),
    PIPELINE_SINK_RUN_FAILURE_KEY("pipeline.sink.run.failure"),
    PIPELINE_SINK_ERROR_TOTAL_KEY("pipeline.sink.error.total"),
    ;
    
    private final String id;

    private static final Map<String, Set<String>> REGISTERED_LABELS;

    static {
        REGISTERED_LABELS = new ConcurrentHashMap<>();
        for (MeterRegistryKey key : MeterRegistryKey.values())
            REGISTERED_LABELS.put(key.name(), new ConcurrentSkipListSet<>());
    }

    MeterRegistryKey(String id)
    {
        this.id = id;
    }

    public String id()
    {
        return this.id;
    }

    /**
     * Creates a complete list of tags out of a provided tag list and a registry of past declared labels.
     * This circumvents an issue with the prometheus client as described here:
     * <ul>
     *     <li><a href="https://github.com/prometheus/client_java/issues/696">prometheus/client_java#696</a></li>
     *     <li><a href="https://github.com/micrometer-metrics/micrometer/issues/4091">micrometer-metering/micrometer#4091</a></li>
     * </ul>
     *
     * @param key the metric key to which tags will be applied
     * @param tags a list of tags to be applied to a metric
     * @return the filled out tag list
     */
    public static List<Tag> fill(String key, Collection<Tag> tags)
    {
        Map<String, String> map = new HashMap<>();
        Set<String> pastLabels = REGISTERED_LABELS.computeIfAbsent(key, k -> new ConcurrentSkipListSet<>());
        for (String label : pastLabels)
            map.put(label, "");
        for (Tag tag : tags)
        {
            map.put(tag.getKey(), tag.getValue());
            pastLabels.add(tag.getKey());
        }
        return map.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).toList();
    }

    public static List<Tag> fill(MeterRegistryKey key, Collection<Tag> tags)
    {
        return fill(key.name(), tags);
    }
}
