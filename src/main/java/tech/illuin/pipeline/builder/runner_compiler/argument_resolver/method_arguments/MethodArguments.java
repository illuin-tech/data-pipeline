package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments;

import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.step.result.ResultView;
import tech.illuin.pipeline.step.result.Results;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record MethodArguments<T, I, P>(
    T object,
    I input,
    P payload,
    Output<P> output,
    ResultView resultView,
    Results results,
    Context<P> context,
    PipelineTag pipelineTag,
    ComponentTag componentTag,
    UIDGenerator uidGenerator
) {}
