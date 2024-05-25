package tech.illuin.pipeline.input.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class MultiAutoIndexer<T> implements MultiIndexer<T>
{
    private static final Logger logger = LoggerFactory.getLogger(MultiAutoIndexer.class);

    @Override
    public Collection<? extends Indexable> resolve(T payload)
    {
        if (payload instanceof Collection<?> collection)
        {
            List<Indexable> indexables = collection.stream()
                .filter(item -> item instanceof Indexable)
                .map(item -> (Indexable) item)
                .toList()
            ;
            if (indexables.isEmpty())
                logger.warn("The provided collection payload did not contain any Indexable");
            return indexables;
        }
        throw new IllegalArgumentException("The provided payload of type " + payload.getClass().getName() + " is not a Collection");
    }
}
