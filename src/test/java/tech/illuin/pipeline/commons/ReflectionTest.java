package tech.illuin.pipeline.commons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReflectionTest
{
    @Test
    public void testIsAnonymousImplementation()
    {
        @SuppressWarnings("Convert2Lambda")
        Function<Integer, Boolean> anonymous = new Function<>() {
            @Override
            public Boolean apply(Integer integer) {
                return true;
            }
        };
        Function<Integer, Boolean> lambda = integer -> true;
        Function<Integer, Boolean> reference = ReflectionTest::returnTrue;
        Function<Integer, Boolean> named = new ReturnTrue();

        Assertions.assertTrue(Reflection.isAnonymousImplementation(anonymous.getClass()));
        Assertions.assertTrue(Reflection.isAnonymousImplementation(lambda.getClass()));
        Assertions.assertTrue(Reflection.isAnonymousImplementation(reference.getClass()));
        Assertions.assertFalse(Reflection.isAnonymousImplementation(named.getClass()));
    }

    @Test
    public void testGetMethodSignature() throws NoSuchMethodException
    {
        String getMethodSignature = Reflection.getMethodSignature(Reflection.class.getMethod("getMethodSignature", Method.class));
        String getOptionalParameter = Reflection.getMethodSignature(Reflection.class.getMethod("getOptionalParameter", Parameter.class, Class.class));

        Assertions.assertEquals("getMethodSignature(Method arg0) -> String", getMethodSignature);
        Assertions.assertEquals("getOptionalParameter(Parameter arg0, Class arg1) -> Optional", getOptionalParameter);
    }

    @Test
    public void testGetOptionalParameter() throws NoSuchMethodException
    {
        Method method = ReflectionTest.class.getMethod("acceptOptional", Optional.class, Optional.class, Object.class);

        Parameter arg0 = method.getParameters()[0];
        Optional<Class<Object>> optionalArg0 = Assertions.assertDoesNotThrow(() -> Reflection.getOptionalParameter(arg0, Object.class));
        Assertions.assertTrue(optionalArg0.isPresent());
        Assertions.assertEquals(Object.class, optionalArg0.get());

        Parameter arg1 = method.getParameters()[1];
        Optional<Class<ReturnTrue>> optionalArg1 = Assertions.assertDoesNotThrow(() -> Reflection.getOptionalParameter(arg1, ReturnTrue.class));
        Assertions.assertTrue(optionalArg1.isPresent());
        Assertions.assertEquals(ReturnTrue.class, optionalArg1.get());

        Optional<Class<Function>> optionalArg1Generic = Assertions.assertDoesNotThrow(() -> Reflection.getOptionalParameter(arg1, Function.class));
        Assertions.assertTrue(optionalArg1Generic.isPresent());
        Assertions.assertEquals(ReturnTrue.class, optionalArg1Generic.get());

        Optional<Class<Integer>> optionalArg1Mismatch = Assertions.assertDoesNotThrow(() -> Reflection.getOptionalParameter(arg1, Integer.class));
        Assertions.assertTrue(optionalArg1Mismatch.isEmpty());
    }

    @Test
    public void testGetOptionalParameter__shouldReturnEmptyForNonOptional() throws NoSuchMethodException
    {
        Method method = ReflectionTest.class.getMethod("acceptOptional", Optional.class, Optional.class, Object.class);
        Parameter arg2 = method.getParameters()[2];

        Optional<Class<?>> optionalArg2 = Assertions.assertDoesNotThrow(() -> Reflection.getOptionalParameter(arg2, Object.class));
        Assertions.assertTrue(optionalArg2.isEmpty());
    }

    @Test
    public void testGetStreamParameter() throws NoSuchMethodException
    {
        Method method = ReflectionTest.class.getMethod("acceptStream", Stream.class, Stream.class, Object.class);

        Parameter arg0 = method.getParameters()[0];
        Optional<Class<Object>> streamArg0 = Assertions.assertDoesNotThrow(() -> Reflection.getStreamParameter(arg0, Object.class));
        Assertions.assertTrue(streamArg0.isPresent());
        Assertions.assertEquals(Object.class, streamArg0.get());

        Parameter arg1 = method.getParameters()[1];
        Optional<Class<ReturnTrue>> streamArg1 = Assertions.assertDoesNotThrow(() -> Reflection.getStreamParameter(arg1, ReturnTrue.class));
        Assertions.assertTrue(streamArg1.isPresent());
        Assertions.assertEquals(ReturnTrue.class, streamArg1.get());

        Optional<Class<Function>> streamArg1Generic = Assertions.assertDoesNotThrow(() -> Reflection.getStreamParameter(arg1, Function.class));
        Assertions.assertTrue(streamArg1Generic.isPresent());
        Assertions.assertEquals(ReturnTrue.class, streamArg1Generic.get());

        Optional<Class<Integer>> streamArg1Mismatch = Assertions.assertDoesNotThrow(() -> Reflection.getStreamParameter(arg1, Integer.class));
        Assertions.assertTrue(streamArg1Mismatch.isEmpty());
    }

    @Test
    public void testGetStreamParameter__shouldReturnEmptyForNonStream() throws NoSuchMethodException
    {
        Method method = ReflectionTest.class.getMethod("acceptStream", Stream.class, Stream.class, Object.class);
        Parameter arg2 = method.getParameters()[2];

        Optional<Class<?>> streamArg2 = Assertions.assertDoesNotThrow(() -> Reflection.getStreamParameter(arg2, Object.class));
        Assertions.assertTrue(streamArg2.isEmpty());
    }

    public static boolean returnTrue(int arg)
    {
        return true;
    }

    public static class ReturnTrue implements Function<Integer, Boolean>
    {
        @Override
        public Boolean apply(Integer integer)
        {
            return true;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void acceptOptional(
        Optional<Object> optionalObject,
        Optional<ReturnTrue> optionalTyped,
        Object nonOptional
    ) {}

    public static void acceptStream(
        Stream<Object> streamObject,
        Stream<ReturnTrue> streamTyped,
        Object nonOptional
    ) {}
}
