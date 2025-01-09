package tech.illuin.pipeline.builder.runner_compiler;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MethodValidator
{
    boolean validate(Method method);

    String description();

    static MethodValidator any()
    {
        return new MethodValidator() {
            @Override
            public boolean validate(Method method)
            {
                return true;
            }

            @Override
            public String description()
            {
                return "`any` condition always passes";
            }
        };
    }

    static MethodValidator and(MethodValidator... validators)
    {
        return new MethodValidator() {
            @Override
            public boolean validate(Method method)
            {
                if (validators.length == 0)
                    return true;
                return Stream.of(validators).allMatch(v -> v.validate(method));
            }

            @Override
            public String description()
            {
                StringBuilder sb = new StringBuilder("`and` condition should match all of:");
                Stream.of(validators).forEach(v -> sb.append("\n").append(" - ").append(v.description()));
                return sb.toString();
            }
        };
    }

    static MethodValidator or(MethodValidator... validators)
    {
        return new MethodValidator() {
            @Override
            public boolean validate(Method method)
            {
                if (validators.length == 0)
                    return true;
                return Stream.of(validators).anyMatch(v -> v.validate(method));
            }

            @Override
            public String description()
            {
                StringBuilder sb = new StringBuilder("`or` condition should match at least one of:");
                Stream.of(validators).forEach(v -> sb.append("\n - ").append(v.description()));
                return sb.toString();
            }
        };
    }
}
