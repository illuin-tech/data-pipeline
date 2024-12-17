package tech.illuin.pipeline.annotation;

import jdk.jfr.Label;

import java.lang.annotation.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Documented
@Inherited
@Label("Context")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Context
{
    String value();
}
