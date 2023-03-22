package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.tsid.TsidCreator;

import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TSIDGenerator implements UIDGenerator
{
    private final Supplier<Tsid> supplier;

    public static final UIDGenerator INSTANCE = new TSIDGenerator();

    public TSIDGenerator()
    {
        this(TsidCreator::getTsid);
    }

    public TSIDGenerator(Supplier<Tsid> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public String generate()
    {
        return this.supplier.get().toString();
    }
}
