package tech.illuin.pipeline.step.result;

import java.util.Arrays;
import java.util.Collection;

public interface MultiResult extends Result
{
    Collection<? extends Result> results();

    static MultiResult of(Result... results)
    {
        return of(Arrays.asList(results));
    }

    static MultiResult of(Collection<? extends Result> results)
    {
        return new AnonymousMultiResult(results);
    }

    record AnonymousMultiResult(
        Collection<? extends Result> results
    ) implements MultiResult {}
}
