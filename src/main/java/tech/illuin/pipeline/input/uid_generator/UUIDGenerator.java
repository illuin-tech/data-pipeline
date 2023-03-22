package tech.illuin.pipeline.input.uid_generator;

import java.util.UUID;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class UUIDGenerator implements UIDGenerator
{
    public static final UIDGenerator INSTANCE = new UUIDGenerator();

    @Override
    public String generate()
    {
        return UUID.randomUUID().toString();
    }
}
