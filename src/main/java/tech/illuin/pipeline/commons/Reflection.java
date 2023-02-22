package tech.illuin.pipeline.commons;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Reflection
{
    private Reflection() {}

    public static boolean isAnonymousImplementation(Class<?> c)
    {
        if (c.isAnonymousClass())
            return true;
        Class<?>[] interfaces = c.getInterfaces();
        return (c.isSynthetic() && interfaces.length > 0 && !interfaces[0].isSynthetic());
    }
}
