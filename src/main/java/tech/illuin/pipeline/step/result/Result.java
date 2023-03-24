package tech.illuin.pipeline.step.result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Result
{
    default String name()
    {
        return this.getClass().getName();
    }
}
