package tech.illuin.pipeline.close;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public interface OnCloseHandler
{
    void execute() throws Exception;
}
