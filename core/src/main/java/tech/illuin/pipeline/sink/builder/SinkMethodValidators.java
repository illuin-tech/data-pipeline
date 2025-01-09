package tech.illuin.pipeline.sink.builder;

import tech.illuin.pipeline.builder.runner_compiler.MethodValidator;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class SinkMethodValidators
{
    public static final MethodValidator validator = MethodValidator.any();

    private SinkMethodValidators() {}
}
