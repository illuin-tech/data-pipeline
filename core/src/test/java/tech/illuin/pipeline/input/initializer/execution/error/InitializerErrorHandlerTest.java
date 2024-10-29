package tech.illuin.pipeline.input.initializer.execution.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.input.indexer.Indexable;

import static java.util.Collections.emptyList;

public class InitializerErrorHandlerTest
{
    @Test
    public void testRethrowAllExcept()
    {
        InitializerErrorHandler handler = InitializerErrorHandler.rethrowAllExcept(
            (exception, context, generator) -> new A("abc", emptyList()),
            IllegalStateException.class,
            IllegalArgumentException.class
        );

        Assertions.assertThrows(RuntimeException.class, () -> handler.handle(new RuntimeException(), null, null));

        Indexable illegalStateResult = Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalStateException(), null, null));
        A illegalStateA = Assertions.assertInstanceOf(A.class, illegalStateResult);
        Assertions.assertEquals("abc", illegalStateA.uid());

        Indexable illegalArgResult = Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalArgumentException(), null, null));
        A illegalArgA = Assertions.assertInstanceOf(A.class, illegalArgResult);
        Assertions.assertEquals("abc", illegalArgA.uid());
    }
}
