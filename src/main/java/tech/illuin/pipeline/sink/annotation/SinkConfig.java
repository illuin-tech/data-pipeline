package tech.illuin.pipeline.sink.annotation;

import tech.illuin.pipeline.sink.execution.error.SinkErrorHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface SinkConfig
{
    String id() default "";
    Class<? extends SinkErrorHandler> errorHandler() default SinkErrorHandler.class;
    boolean async() default false;
}
