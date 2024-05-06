package tech.illuin.pipeline.execution.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineException;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.SimpleContext;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.input.author_resolver.AuthorResolver;
import tech.illuin.pipeline.input.uid_generator.UUIDGenerator;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.output.factory.DefaultOutputFactory;

import java.util.List;

public class PipelineErrorHandlerTest
{
    @Test
    public void shouldWrapChecked()
    {
        PipelineErrorHandler handler = PipelineErrorHandler::wrapChecked;

        Assertions.assertThrows(PipelineException.class, () -> handler.handle(new Exception("checked"), null, null, null));
        Assertions.assertThrows(CustomRuntimeException.class, () -> handler.handle(new CustomRuntimeException(), null, null, null));
    }

    @Test
    public void shouldWrap()
    {
        PipelineErrorHandler handler0 = PipelineErrorHandlerTest::handler0;
        PipelineErrorHandler handler1 = PipelineErrorHandlerTest::handler1;

        Exception ex = new Exception("test-message");
        Output out = Assertions.assertDoesNotThrow(() -> handler0.andThen(handler1).handle(ex, null, null, null));

        Assertions.assertEquals(ex.getMessage(), out.tag().pipeline());
    }

    @Test
    public void shouldTransferPreviousState() throws Exception
    {
        Pipeline<String> recovery = Pipeline.<String>of("recovery")
            .registerStep((in, res, ctx) -> new TestResult("def", "ko"))
            .build()
        ;

        PipelineErrorHandler errorHandler = (exception, previous, input, context) -> recovery.run(
            (String) input,
            new SimpleContext(previous).copyFrom(context)
        );
        Pipeline<String> pipeline = Pipeline.<String>of("pipeline")
            .registerStep((in, res, ctx) -> new TestResult("abc", "ok"))
            .registerStep((in, res, ctx) -> { throw new RuntimeException("Random error"); })
            .setErrorHandler(errorHandler)
            .build()
        ;

        List<TestResult> results = Assertions.assertDoesNotThrow(() -> pipeline.run("anything").results().stream(TestResult.class).toList());

        Assertions.assertEquals("abc", results.get(0).name());
        Assertions.assertEquals("def", results.get(1).name());

        pipeline.close();
        recovery.close();
    }

    private static Output handler0(Exception exception, Output previous, Object input, Context context) throws PipelineException
    {
        throw new PipelineException(exception.getMessage(), exception);
    }

    private static Output handler1(Exception exception, Output previous, Object input, Context context)
    {
        return new DefaultOutputFactory<>().create(
            new PipelineTag(UUIDGenerator.INSTANCE.generate(), exception.getMessage(), AuthorResolver.ANONYMOUS),
            null,
            null,
            new SimpleContext()
        );
    }

    private static class CustomRuntimeException extends RuntimeException
    {
        public CustomRuntimeException() { super(); }
    }
}
