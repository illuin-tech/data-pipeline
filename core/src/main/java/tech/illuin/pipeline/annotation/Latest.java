package tech.illuin.pipeline.annotation;

import jdk.jfr.Label;

import java.lang.annotation.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Documented
@Inherited
@Label("Latest")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Latest
{
    String name() default "";

    boolean self() default false;
}
