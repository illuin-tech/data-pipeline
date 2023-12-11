package tech.illuin.pipeline.step.execution.condition;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.Indexable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MetadataCondition implements StepCondition
{
    private final Map<String, Object> conditions;

    public MetadataCondition(String key, Object value)
    {
        this.conditions = Map.of(key, value);
    }

    public MetadataCondition(Map<String, Object> conditions)
    {
        this.conditions = new HashMap<>(conditions);
    }

    @Override
    public boolean canExecute(Indexable indexable, Context<?> context)
    {
        for (Map.Entry<String, Object> entry : this.conditions.entrySet())
        {
            Optional<Object> metadata = context.get(entry.getKey());
            if (metadata.isEmpty() || !metadata.get().equals(entry.getValue()))
                return false;
        }
        return true;
    }
}
