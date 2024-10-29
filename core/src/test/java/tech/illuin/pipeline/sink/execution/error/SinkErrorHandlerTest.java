package tech.illuin.pipeline.sink.execution.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.step.execution.error.StepErrorHandler;
import tech.illuin.pipeline.step.result.Result;

public class SinkErrorHandlerTest
{
    @Test
    public void testRethrowAllExcept()
    {
        SinkErrorHandler handler = SinkErrorHandler.rethrowAllExcept(
            IllegalStateException.class,
            IllegalArgumentException.class
        );

        Assertions.assertThrows(RuntimeException.class, () -> handler.handle(new RuntimeException(), null, null));

        Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalStateException(), null, null));

        Assertions.assertDoesNotThrow(() -> handler.handle(new IllegalArgumentException(), null, null));
    }
}
