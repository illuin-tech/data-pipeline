package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface StepAssembler<T extends Indexable, I>
{
    void assemble(StepBuilder<T, I> builder);

    default StepDescriptor<T, I> build(StepBuilder<T, I> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
