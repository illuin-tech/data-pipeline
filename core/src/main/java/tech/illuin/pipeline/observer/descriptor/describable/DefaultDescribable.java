package tech.illuin.pipeline.observer.descriptor.describable;

public class DefaultDescribable implements Describable
{
    private final Object value;

    public DefaultDescribable(Object value)
    {
        this.value = value;
    }

    @Override
    public Object describe()
    {
        if (this.value == null)
            return null;
        return this.value.getClass().getName();
    }
}
