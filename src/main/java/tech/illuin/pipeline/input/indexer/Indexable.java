package tech.illuin.pipeline.input.indexer;

import com.github.f4b6a3.ksuid.KsuidCreator;

import java.time.Instant;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Indexable
{
    String PIPELINE_PREFIX = "px";
    String RESULT_PREFIX = "rx";

    String uid();

    static String generateUid()
    {
        return KsuidCreator.getSubsecondKsuid().toString().toLowerCase();
    }

    static String generateUid(Instant instant)
    {
        return KsuidCreator.getSubsecondKsuid(instant).toString().toLowerCase();
    }
}
