package tech.illuin.pipeline.builder.runner_compiler.argument_resolver.mapper_factory;

import tech.illuin.pipeline.builder.runner_compiler.argument_resolver.method_arguments.MethodArguments;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MethodArgumentMapper<T, I, P>
{
    Object map(MethodArguments<T, I, P> arguments);
}
