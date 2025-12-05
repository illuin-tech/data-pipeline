package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments;

import tech.illuin.pipeline.step.result.Result;

public record MissingArgument(String message) implements Result {}
