package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.MarkerManager;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record MethodArguments<T, I>(
    T object,
    I input,
    Object payload,
    Output output,
    ResultView resultView,
    Results results,
    Context context,
    PipelineTag pipelineTag,
    ComponentTag componentTag,
    UIDGenerator uidGenerator,
    MarkerManager markerManager
) {}
