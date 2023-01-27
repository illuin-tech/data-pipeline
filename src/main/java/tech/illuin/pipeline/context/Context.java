package tech.illuin.pipeline.context;

import tech.illuin.pipeline.output.Output;

import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Context<P>
{
    Optional<Output<P>> parent();
}
