package tech.illuin.pipeline.input.uid_generator;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface UIDGenerator
{
    /**
     * This method should return a unique identifier of some type.
     *
     * @return
     */
    String generate();

    default String generate(Instant creationTime)
    {
        return this.generate();
    }
}
