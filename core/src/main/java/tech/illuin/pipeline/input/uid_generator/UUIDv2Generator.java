package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.enums.UuidLocalDomain;

public class UUIDv2Generator implements UIDGenerator
{
    private final UuidLocalDomain localDomain;
    private final int localIdentifier;

    public UUIDv2Generator(UuidLocalDomain localDomain, int localIdentifier)
    {
        this.localDomain = localDomain;
        this.localIdentifier = localIdentifier;
    }

    @Override
    public String generate()
    {
        return UuidCreator.getDceSecurity(this.localDomain, this.localIdentifier).toString();
    }
}
