package tech.illuin.pipeline.generic;

import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.result.Result;

import java.time.Duration;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Tests
{
    private Tests() {}

    public static List<String> getResultTypes(Output output, Indexable object)
    {
        return output.results(object).stream().map(Result::name).sorted().toList();
    }

    public static List<String> getResultTypes(Output output)
    {
        return output.results().stream().map(Result::name).sorted().toList();
    }

    public static void sleep(Duration duration)
    {
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
