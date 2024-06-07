package tech.illuin.pipeline.input.initializer.builder;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface InitializerAssembler<I>
{
    void assemble(InitializerBuilder<I> builder);

    default InitializerDescriptor<I> build(InitializerBuilder<I> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
