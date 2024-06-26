package tech.illuin.pipeline.output;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.indexer.IndexContainer;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.step.result.ResultContainer;
import tech.illuin.pipeline.step.result.Results;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class Output implements Comparable<Output>
{
    private final PipelineTag tag;
    private final Instant createdAt;
    private final IndexContainer index;
    private final ResultContainer results;
    private final Context context;
    private final Object payload;
    private Instant finishedAt;

    public Output(PipelineTag tag, Object payload, Context context)
    {
        this(tag, Instant.now(), payload, context);
    }

    public Output(PipelineTag tag, Instant createdAt, Object payload, Context context)
    {
        this.tag = tag;
        this.payload = payload;
        this.context = context;
        this.createdAt = createdAt;
        this.index = new IndexContainer();
        this.results = new ResultContainer();
        context.parent().ifPresent(parent -> this.results().register(parent.results()));
    }

    public PipelineTag tag()
    {
        return this.tag;
    }

    public Instant createdAt()
    {
        return this.createdAt;
    }

    public synchronized Output finish()
    {
        if (this.finishedAt == null)
            this.finishedAt = Instant.now();
        return this;
    }

    public Optional<Instant> finishedAt()
    {
        return Optional.ofNullable(this.finishedAt);
    }

    public Object payload()
    {
        return this.payload;
    }

    public <T> T payload(Class<T> type)
    {
        if (!type.isInstance(this.payload))
            throw new IllegalArgumentException("The requested type does not match that of the output's payload (" + getClassName(this.payload) + ")");
        //noinspection unchecked
        return (T) this.payload;
    }

    public Context context()
    {
        return this.context;
    }

    public IndexContainer index()
    {
        return this.index;
    }

    public ResultContainer results()
    {
        return this.results;
    }

    public Results results(Indexable indexable)
    {
        return this.results.of(indexable);
    }

    public <T> Results results(SingleIndexer<T> indexer, Class<T> type)
    {
        return this.results.view(indexer.resolve(this.payload(type)));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Output output = (Output) o;
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

    private static String getClassName(Object arg)
    {
        if (arg == null)
            return null;
        return arg.getClass().getName();
    }
}
