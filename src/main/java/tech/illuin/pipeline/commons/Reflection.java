package tech.illuin.pipeline.commons;

import tech.illuin.pipeline.step.result.Result;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Reflection
{
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = Map.of(
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        char.class, Character.class,
        boolean.class, Boolean.class,
        void.class, Void.class
    );

    private Reflection() {}

    public static boolean isAnonymousImplementation(Class<?> c)
    {
        if (c.isAnonymousClass())
            return true;
        Class<?>[] interfaces = c.getInterfaces();
        return (c.isSynthetic() && interfaces.length > 0 && !interfaces[0].isSynthetic());
    }

    public static String getMethodSignature(Method method)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getName());
        builder.append("(");
        builder.append(Stream.of(method.getParameters())
            .map(p -> p.getType().getSimpleName() + " " + p.getName())
            .collect(Collectors.joining(", "))
        );
        builder.append(") -> ");
        builder.append(method.getReturnType().getSimpleName());
        return builder.toString();
    }

    public static <C extends Class<?>> Optional<C> getOptionalParameter(Parameter parameter, C expectedSuperclass)
    {
        if (!parameter.getType().equals(Optional.class))
            return Optional.empty();

        Type[] typeParams = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
        if (typeParams.length != 1)
            throw new IllegalStateException("Argument of type Optional of a " + expectedSuperclass.getSimpleName() + " subtype are expected to be declared with a single explicit bound");

        Type typeParam = typeParams[0];
        if (!(typeParam instanceof Class<?> typeParamClass))
            throw new IllegalStateException("Argument of type Optional uses an unexpected type " + typeParam.getClass());

        //noinspection unchecked
        return expectedSuperclass.isAssignableFrom(typeParamClass)
            ? Optional.of((C) typeParamClass)
            : Optional.empty()
        ;
    }

    public static <C extends Class<?>> Optional<C> getStreamParameter(Parameter parameter, C expectedSuperclass)
    {
        if (!parameter.getType().equals(Stream.class))
            return Optional.empty();

        Type[] typeParams = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments();
        if (typeParams.length != 1)
            throw new IllegalStateException("Argument of type Stream of a " + expectedSuperclass.getSimpleName() + " subtype are expected to be declared with a single explicit bound");

        Type typeParam = typeParams[0];
        if (!(typeParam instanceof Class<?> typeParamClass))
            throw new IllegalStateException("Argument of type Stream uses an unexpected type " + typeParam.getClass());

        //noinspection unchecked
        return Result.class.isAssignableFrom(typeParamClass)
            ? Optional.of((C) typeParamClass)
            : Optional.empty()
        ;
    }

    public static Class<?> getWrapperType(Class<?> primitiveType)
    {
        if (!primitiveType.isPrimitive())
            throw new IllegalArgumentException("The provided type is not a primitive type");

        return PRIMITIVE_MAP.get(primitiveType);
    }
}
