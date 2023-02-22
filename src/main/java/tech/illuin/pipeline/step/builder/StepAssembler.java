package tech.illuin.pipeline.step.builder;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface StepAssembler<T extends Indexable, I, P>
{
    void assemble(StepBuilder<T, I, P> builder);

    default StepDescriptor<T, I, P> build(StepBuilder<T, I, P> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
