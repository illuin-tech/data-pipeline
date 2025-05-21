package tech.illuin.pipeline.metering.tag;

public enum MetricScope
{
    /** MARKER scoped tags will be used in MDC and other potentially high-cardinality scenarios */
    MARKER,
    /** TAG scoped tags will be used as Micrometer metric tags and should avoid high-cardinality scenarios */
    TAG,
}
