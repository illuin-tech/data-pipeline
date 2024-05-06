package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.Optional;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Context
{
    Optional<Output> parent();

    Context set(String key, Object value);

    Context copyFrom(Context other);

    boolean has(String key);

    Set<String> keys();

    Optional<Object> get(String key);

    <T> Optional<T> get(String key, Class<T> type);
}
