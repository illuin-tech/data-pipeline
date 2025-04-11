package tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.SinkHandler;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public interface RetrySinkHandler extends SinkHandler
{
    void onRetry(Output output, Context context);

    RetrySinkHandler NOOP = new RetrySinkHandler() {
        @Override
        public void onError(Output output, Context context, Exception e) {}

        @Override
        public void onSuccess(Output output, Context context) {}

        @Override
        public void onRetry(Output output, Context context) {}
    };
}
