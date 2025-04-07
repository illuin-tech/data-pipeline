package tech.illuin.pipeline.resilience4j.execution.wrapper.config.circuitbreaker;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.resilience4j.execution.wrapper.config.SinkHandler;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public interface CircuitBreakerSinkHandler extends SinkHandler
{
    CircuitBreakerSinkHandler NOOP = new CircuitBreakerSinkHandler() {
        @Override
        public void onError(Output output, Context context, Exception e) {}

        @Override
        public void onSuccess(Output output, Context context) {}
    };
}
