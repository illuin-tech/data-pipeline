package tech.illuin.pipeline.output;

import tech.illuin.pipeline.input.indexer.IndexContainer;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultContainer;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class Output<T> implements Comparable<Output<T>>
{
    private final PipelineTag tag;
    private final String author;
    private final Instant createdAt;
    private final IndexContainer index;
    private final ResultContainer results;
    private T payload;

    public Output(String pipeline, String author)
    {
        this.tag = new PipelineTag(UUID.randomUUID(), pipeline);
        this.author = author;
        this.createdAt = Instant.now();
        this.index = new IndexContainer();
        this.results = new ResultContainer();
    }

    public PipelineTag tag()
    {
        return this.tag;
    }

    public String author()
    {
        return this.author;
    }

    public Instant createdAt()
    {
        return this.createdAt;
    }

    public T payload()
    {
        return this.payload;
    }

    public Output<T> setPayload(T payload)
    {
        this.payload = payload;
        return this;
    }

    public IndexContainer index()
    {
        return this.index;
    }

    public ResultContainer results()
    {
        return this.results;
    }

    public Stream<Result> results(Indexable indexable)
    {
        return this.results.stream(indexable);
    }

    public Stream<Result> results(SingleIndexer<T> indexer)
    {
        return this.results.stream(indexer.resolve(this.payload()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Output<?> output = (Output<?>) o;
        return this.tag.equals(output.tag);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.tag.uid());
    }

    @Override
    public int compareTo(Output o)
    {
        return this.tag.uid().compareTo(o.tag.uid());
    }
}
