package tech.illuin.pipeline.input.indexer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexContainer
{
    private final Map<String, Entry<?>> index;

    public IndexContainer()
    {
        this.index = new HashMap<>();
    }

    public boolean contains(String uid)
    {
        return this.index.containsKey(uid);
    }

    public boolean contains(Indexable indexable)
    {
        return this.index.containsKey(indexable.uid());
    }

    public Optional<Object> get(String uid)
    {
        return Optional.ofNullable(this.index.get(uid)).map(Entry::data);
    }

    public <T> Optional<T> get(String uid, Class<T> type)
    {
        Entry<?> entry = this.index.get(uid);
        if (entry != null && !type.isAssignableFrom(entry.type()))
            throw new IllegalArgumentException("The requested type " + type.getName() + " is not assignable to the declared type " + entry.type().getName() + " found for entry " + uid);
        return Optional.ofNullable(entry).map(e -> type.cast(entry.data()));
    }

    public Stream<Indexable> stream()
    {
        return this.index.values().stream()
            .sorted(Comparator.comparingInt(Entry::position))
            .map(Entry::data)
        ;
    }

    public IndexContainer index(Indexable data)
    {
        if (data == null)
            throw new IllegalArgumentException("An Indexer returned a null reference.");

        Entry<Indexable> entry = new Entry<>(data, data.getClass(), this.index.size());
        this.index.put(data.uid(), entry);
        return this;
    }

    public record Entry<T extends Indexable>(
        T data,
        Class<? extends T> type,
        int position
    ) {}
}
