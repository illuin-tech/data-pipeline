package tech.illuin.pipeline.sink.builder;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface SinkAssembler
{
    void assemble(SinkBuilder builder);

    default SinkDescriptor build(SinkBuilder builder)
    {
        this.assemble(builder);
        return builder.build();
    }
}
