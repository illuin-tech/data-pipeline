package tech.illuin.pipeline.context;

import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Optional;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ComponentContext implements LocalContext
{
    private final Context globalContext;
    private final Object input;
    private final ComponentTag componentTag;
    private final UIDGenerator uidGenerator;
    private final MarkerManager markerManager;

    public ComponentContext(
        Context globalContext,
        Object input,
        ComponentTag componentTag,
        UIDGenerator uidGenerator,
        MarkerManager markerManager
    ) {
        this.globalContext = globalContext;
        this.input = input;
        this.componentTag = componentTag;
        this.uidGenerator = uidGenerator;
        this.markerManager = markerManager;
    }

    @Override
    public Object input()
    {
        return this.input;
    }

    @Override
    public PipelineTag pipelineTag()
    {
        return this.componentTag.pipelineTag();
    }

    @Override
    public ComponentTag componentTag()
    {
        return this.componentTag;
    }

    @Override
    public UIDGenerator uidGenerator()
    {
        return this.uidGenerator;
    }

    @Override
    public MarkerManager markerManager()
    {
        return this.markerManager;
    }

    @Override
    public Optional<Output> parent()
    {
        return this.globalContext.parent();
    }

    @Override
    public Context set(String key, Object value)
    {
        return this.globalContext.set(key, value);
    }

    @Override
    public Context copyFrom(Context other)
    {
        return this.globalContext.copyFrom(other);
    }

    @Override
    public boolean has(String key)
    {
        return this.globalContext.has(key);
    }

    @Override
    public Set<String> keys()
    {
        return this.globalContext.keys();
    }

    @Override
    public Optional<Object> get(String key)
    {
        return this.globalContext.get(key);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type)
    {
        return this.globalContext.get(key, type);
    }
}
