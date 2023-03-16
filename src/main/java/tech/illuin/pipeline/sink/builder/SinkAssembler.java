package tech.illuin.pipeline.sink.builder;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface SinkAssembler<P>
{
    void assemble(SinkBuilder<P> builder);

    default SinkDescriptor<P> build(SinkBuilder<P> builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
