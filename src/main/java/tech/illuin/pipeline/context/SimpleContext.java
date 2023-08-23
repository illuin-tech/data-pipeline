package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SimpleContext<P> implements Context<P>
{
    private final Output<P> parent;
    private final Map<String, Object> metadata;

    public SimpleContext()
    {
        this(null);
    }

    public SimpleContext(Output<P> parent)
    {
        this.parent = parent;
        this.metadata = new HashMap<>();
    }

    @Override
    public Optional<Output<P>> parent()
    {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Context<P> set(String key, Object value)
    {
        this.metadata.put(key, value);
        return this;
    }

    @Override
    public Context<P> copyFrom(Context<P> other)
    {
        for (String key : other.keys())
            this.set(key, other.get(key).orElse(null));
        return this;
    }

    @Override
    public boolean has(String key)
    {
        return this.metadata.containsKey(key);
    }

    @Override
    public Set<String> keys()
    {
        return this.metadata.keySet();
    }

    @Override
    public Optional<Object> get(String key)
    {
        Object result = this.metadata.get(key);
        return Optional.ofNullable(result);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type)
    {
        return this.get(key).map(type::cast);
    }
}
