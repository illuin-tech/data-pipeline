package tech.illuin.pipeline.generic;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Tests
{
    private Tests() {}

    public static List<String> getResultTypes(Output<?> output, Indexable object)
    {
        return output.results(object).stream().map(Result::name).sorted().toList();
    }
}
