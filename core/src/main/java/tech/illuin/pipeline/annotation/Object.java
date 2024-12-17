package tech.illuin.pipeline.annotation;

import jdk.jfr.Label;

import java.lang.annotation.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Documented
@Inherited
@Label("Object")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Object
{

}
