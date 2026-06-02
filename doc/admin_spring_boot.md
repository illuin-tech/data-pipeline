# Spring Boot Admin

The Spring Boot integration provides a seamless way to add the admin interface to your Spring Boot application.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tech.illuin</groupId>
    <artifactId>data-pipeline-admin-spring-boot</artifactId>
    <version>0.31</version>
</dependency>
```

## Usage

To enable the admin interface, add the `@EnablePipelineAdmin` annotation to one of your configuration classes.

```java
@SpringBootApplication
@EnablePipelineAdmin
public class MyApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(MyApplication.class, args);
    }

    @Bean
    public Pipeline<?> myPipeline()
    {
        return Pipeline.of("my-pipeline").build();
    }
}
```

The admin will automatically discover all `Pipeline` and `PipelineProvider` beans in the application context.

## Configuration

You can configure the admin interface using properties in your `application.yaml` or `application.properties`:

```yaml
data-pipeline:
  admin:
    enabled: true # defaults to true
    path: "/pipeline-admin" # defaults to /pipeline-admin
    include: # defaults to an empty list
      - p1
      - p2
    exclude: # defaults to an empty list
      - p3
```

| Property                      | Description                              | Default           |
|-------------------------------|------------------------------------------|-------------------|
| `data-pipeline.admin.enabled` | Enables or disables the admin interface. | `true`            |
| `data-pipeline.admin.path`    | The base path for the admin interface.   | `/pipeline-admin` |
| `data-pipeline.admin.include` | List of pipeline IDs to include.         |                   |
| `data-pipeline.admin.exclude` | List of pipeline IDs to exclude.         |                   |
