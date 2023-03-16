package tech.illuin.pipeline.input.initializer.builder;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface InitializerAssembler<I, P>
{
    void assemble(InitializerBuilder<I, P> builder);

    default InitializerDescriptor<I, P> build(InitializerBuilder<I, P> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
