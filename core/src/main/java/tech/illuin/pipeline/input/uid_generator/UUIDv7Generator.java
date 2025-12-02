package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.uuid.UuidCreator;

public class UUIDv7Generator implements UIDGenerator
{
    public static final UIDGenerator INSTANCE = new UUIDv7Generator();

    @Override
    public String generate()
    {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
}
