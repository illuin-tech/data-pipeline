package tech.illuin.pipeline.resilience4j.execution.wrapper.config;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.output.Output;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public interface SinkHandler
{
    void onError(Output output, Context context, Exception e);

    void onSuccess(Output output, Context context);
}
