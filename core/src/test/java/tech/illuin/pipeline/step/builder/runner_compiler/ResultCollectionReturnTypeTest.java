package tech.illuin.pipeline.step.builder.runner_compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class ResultCollectionReturnTypeTest
{
    private static final ResultCollectionReturnType validator = new ResultCollectionReturnType();

    @Test
    public void test_nonCollection_shouldFail()
    {
        Method method = getMethod("nonCollection");
        Assertions.assertFalse(validator.validate(method));
    }

    @Test
    public void test_invalidGenericCollection_shouldFail()
    {
        Method method = getMethod("invalidGenericCollection");
        Assertions.assertFalse(validator.validate(method));
    }

    private static Method getMethod(String name)
    {
        try {
            return ResultCollectionReturnTypeTest.class.getDeclaredMethod(name);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Integer nonCollection()
    {
        return 1;
    }

    private static List<Integer> invalidGenericCollection()
    {
        return List.of(1, 2, 3);
    }
}
