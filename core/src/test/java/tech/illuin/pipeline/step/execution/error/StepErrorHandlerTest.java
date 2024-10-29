package tech.illuin.pipeline.step.execution.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.Results;

import static java.util.Collections.emptyList;

public class StepErrorHandlerTest
{
    @Test
    public void testRethrowAllExcept()
    {
        StepErrorHandler handler = StepErrorHandler.rethrowAllExcept(
            (exception, input, payload, results, context) -> new TestResult("abc", "def"),
            IllegalStateException.class,
            IllegalArgumentException.class
        );

        Assertions.assertThrows(RuntimeException.class, () -> handler.handle(new RuntimeException(), null, null, null, null));

        Result illegalStateResult = Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalStateException(), null, null, null, null));
        TestResult illegalStateA = Assertions.assertInstanceOf(TestResult.class, illegalStateResult);
        Assertions.assertEquals("abc", illegalStateA.name());

        Result illegalArgResult = Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalArgumentException(), null, null, null, null));
        TestResult illegalArgA = Assertions.assertInstanceOf(TestResult.class, illegalArgResult);
        Assertions.assertEquals("abc", illegalArgA.name());
    }
}
