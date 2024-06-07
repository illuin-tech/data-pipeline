package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.ksuid.Ksuid;
import com.github.f4b6a3.ksuid.KsuidCreator;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class KSUIDGenerator implements UIDGenerator
{
    private final Supplier<Ksuid> supplier;

    public static final UIDGenerator INSTANCE = new KSUIDGenerator();

    public KSUIDGenerator()
    {
        this(KsuidCreator::getSubsecondKsuid);
    }

    public KSUIDGenerator(Supplier<Ksuid> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public String generate()
    {
        return this.supplier.get().toString().toLowerCase();
    }

    @Override
    public String generate(Instant creationTime)
    {
        return KsuidCreator.getSubsecondKsuid(creationTime).toString().toLowerCase();
    }
}
