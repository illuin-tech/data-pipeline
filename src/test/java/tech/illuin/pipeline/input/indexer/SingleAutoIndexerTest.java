package tech.illuin.pipeline.input.indexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.uid_generator.UUIDGenerator;

import java.util.List;

public class SingleAutoIndexerTest
{
    @Test
    public void testIndexer()
    {
        IndexContainer container = new IndexContainer();
        Indexer<MyIndexable> indexer = new SingleAutoIndexer<>();
        MyIndexable payload = new MyIndexable(UUIDGenerator.INSTANCE.generate());

        indexer.index(payload, container);

        Assertions.assertEquals(1, container.stream().count());
        Assertions.assertEquals(payload, container.get(payload.uid()).orElseThrow());
    }

    public record MyIndexable(
        String uid
    ) implements Indexable {};
}
