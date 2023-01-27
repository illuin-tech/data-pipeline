package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SimpleContext<P> implements Context<P>
{
    private final Output<P> parent;

    public SimpleContext()
    {
        this(null);
    }

    public SimpleContext(Output<P> parent)
    {
        this.parent = parent;
    }

    @Override
    public Optional<Output<P>> parent()
    {
        return Optional.ofNullable(this.parent);
    }
}
