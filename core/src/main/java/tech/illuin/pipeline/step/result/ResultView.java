package tech.illuin.pipeline.step.result;

import tech.illuin.pipeline.input.indexer.Indexable;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ResultView extends Results
{
    ScopedResults self();

    ScopedResults of(Indexable indexable);

    ScopedResults of(String uid);
}
