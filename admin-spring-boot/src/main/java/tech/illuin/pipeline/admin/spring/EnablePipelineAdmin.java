package tech.illuin.pipeline.admin.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PipelineAdminAutoConfiguration.class)
public @interface EnablePipelineAdmin
{
}
