package tech.illuin.pipeline.input.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@FunctionalInterface
public non-sealed interface SingleIndexer<T> extends Indexer<T>
{
    Logger logger = LoggerFactory.getLogger(SingleIndexer.class);

    Indexable resolve(T payload);

    @Override
    default void index(T payload, IndexContainer container)
    {
        Indexable indexable = this.resolve(payload);
        if (indexable != null)
            container.index(indexable);
        else
            logger.debug("Indexing from {} skipped as it returned a null value", this.getClass().getName());
    }

    static <P extends Indexable> SingleIndexer<P> auto()
    {
        return p -> p;
    }
}
