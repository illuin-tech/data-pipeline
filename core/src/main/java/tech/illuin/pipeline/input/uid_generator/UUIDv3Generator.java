package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.enums.UuidNamespace;

public class UUIDv3Generator implements UIDGenerator
{
    private final UuidNamespace namespace;
    private final String url;

    public UUIDv3Generator(UuidNamespace namespace, String url)
    {
        this.namespace = namespace;
        this.url = url;
    }

    @Override
    public String generate()
    {
        return UuidCreator.getNameBasedMd5(this.namespace, this.url).toString();
    }
}
