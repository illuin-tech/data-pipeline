package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Context<P>
{
    Optional<Output<P>> parent();

    Context<P> set(String key, Object value);

    boolean has(String key);

    Optional<Object> get(String key);

    <T> Optional<T> get(String key, Class<T> type);
}
