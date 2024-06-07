package tech.illuin.pipeline.input.indexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.uid_generator.UUIDGenerator;

import java.util.List;

public class MultiAutoIndexerTest
{
    @Test
    public void testIndexer()
    {
        IndexContainer container = new IndexContainer();
        Indexer<List<MyIndexable>> indexer = new MultiAutoIndexer<>();
        List<MyIndexable> payload = List.of(
            new MyIndexable(UUIDGenerator.INSTANCE.generate()),
            new MyIndexable(UUIDGenerator.INSTANCE.generate())
        );

        indexer.index(payload, container);

        Assertions.assertEquals(2, container.stream().count());
        Assertions.assertEquals(payload.get(0), container.get(payload.get(0).uid()).orElseThrow());
        Assertions.assertEquals(payload.get(1), container.get(payload.get(1).uid()).orElseThrow());
    }

    public record MyIndexable(
        String uid
    ) implements Indexable {};
}
