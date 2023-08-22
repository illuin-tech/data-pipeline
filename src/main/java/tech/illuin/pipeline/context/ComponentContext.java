package tech.illuin.pipeline.context;

import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ComponentContext<P> implements LocalContext<P>
{
    private final Context<P> globalContext;
    private final Object input;
    private final PipelineTag pipelineTag;
    private final ComponentTag componentTag;
    private final UIDGenerator uidGenerator;
    private final LogMarker marker;

    public ComponentContext(
        Context<P> globalContext,
        Object input,
        PipelineTag pipelineTag,
        ComponentTag componentTag,
        UIDGenerator uidGenerator,
        LogMarker marker
    ) {
        this.globalContext = globalContext;
        this.input = input;
        this.pipelineTag = pipelineTag;
        this.componentTag = componentTag;
        this.uidGenerator = uidGenerator;
        this.marker = marker;
    }

    @Override
    public Object input()
    {
        return this.input;
    }

    @Override
    public PipelineTag pipelineTag()
    {
        return this.pipelineTag;
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
    public LogMarker logMarker()
    {
        return this.marker;
    }

    @Override
    public Optional<Output<P>> parent()
    {
        return this.globalContext.parent();
    }

    @Override
    public Context<P> set(String key, Object value)
    {
        return this.globalContext.set(key, value);
    }

    @Override
    public boolean has(String key)
    {
        return this.globalContext.has(key);
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
