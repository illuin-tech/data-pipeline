package tech.illuin.pipeline.input.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public non-sealed interface MultiIndexer<T> extends Indexer<T>
{
    Logger logger = LoggerFactory.getLogger(MultiIndexer.class);

    Collection<? extends Indexable> resolve(T payload);

    @Override
    default void index(T payload, IndexContainer container)
    {
        this.resolve(payload).forEach(indexable -> {
            if (indexable != null)
                container.index(indexable);
            else
                logger.debug("Indexing from {} skipped as it returned a null value", this.getClass().getName());
        });
    }

    static <P extends Indexable> SingleIndexer<P> auto()
    {
        return p -> p;
    }
}
