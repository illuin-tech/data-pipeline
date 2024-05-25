package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ULIDGenerator implements UIDGenerator
{
    private final Supplier<Ulid> supplier;

    public static final UIDGenerator INSTANCE = new ULIDGenerator();

    public ULIDGenerator()
    {
        this(UlidCreator::getUlid);
    }

    public ULIDGenerator(Supplier<Ulid> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public String generate()
    {
        return this.supplier.get().toString();
    }
}
