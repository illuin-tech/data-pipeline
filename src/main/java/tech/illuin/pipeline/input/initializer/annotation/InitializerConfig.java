package tech.illuin.pipeline.input.initializer.annotation;

import tech.illuin.pipeline.input.initializer.execution.error.InitializerErrorHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
public @interface InitializerConfig
{
    String id() default "";
    Class<? extends InitializerErrorHandler> errorHandler() default InitializerErrorHandler.class;
}
