package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.Optional;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Context<P>
{
    Optional<Output<P>> parent();

    Context<P> set(String key, Object value);

    Context<P> copyFrom(Context<P> other);

    boolean has(String key);

    Set<String> keys();

    Optional<Object> get(String key);

    <T> Optional<T> get(String key, Class<T> type);
}
