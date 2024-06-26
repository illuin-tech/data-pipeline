package tech.illuin.pipeline.builder.runner_compiler;

import java.lang.annotation.Annotation;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface RunnerCompiler<C extends Annotation, T, I>
{
    CompiledMethod<C, T, I> compile(Object target);
}
