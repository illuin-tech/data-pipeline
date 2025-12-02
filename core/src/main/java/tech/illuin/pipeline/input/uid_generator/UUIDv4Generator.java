package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.uuid.UuidCreator;

public class UUIDv4Generator implements UIDGenerator
{
    public static final UIDGenerator INSTANCE = new UUIDv4Generator();

    @Override
    public String generate()
    {
        return UuidCreator.getRandomBased().toString();
    }
}
