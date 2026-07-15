# Micronaut Admin

The Micronaut integration provides a seamless way to add the admin interface to your Micronaut application.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tech.illuin</groupId>
    <artifactId>data-pipeline-admin-micronaut</artifactId>
    <version>0.32</version>
</dependency>
```

## Usage

The admin interface is automatically enabled if the dependency is present and the package is scanned. It will automatically discover all `Pipeline` and `PipelineProvider` beans.

```java
@Factory
public class MyPipelines
{
    @Singleton
    public Pipeline<?> myPipeline()
    {
        return Pipeline.of("my-pipeline").build();
    }
}
```

## Configuration

You can configure the admin interface using properties in your `application.yaml`:

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
