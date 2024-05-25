package tech.illuin.pipeline.builder.runner_compiler;

import java.lang.reflect.Method;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MethodValidator
{
    void validate(Method method);
}
