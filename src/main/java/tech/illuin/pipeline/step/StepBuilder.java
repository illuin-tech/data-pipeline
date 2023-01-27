package tech.illuin.pipeline.step;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface StepBuilder<T extends Indexable, I, P>
{
    void assemble(Step.Builder<T, I, P> builder);

    default Step<T, I, P> build(Step.Builder<T, I, P> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
