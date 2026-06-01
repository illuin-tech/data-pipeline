# Quarkus Admin

The Quarkus integration provides a seamless way to add the admin interface to your Quarkus application.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tech.illuin</groupId>
    <artifactId>data-pipeline-admin-quarkus</artifactId>
    <version>0.31</version>
</dependency>
```

## Usage

The admin interface is automatically enabled if the dependency is present. It will automatically discover all `Pipeline` and `PipelineProvider` beans.

```java
@ApplicationScoped
public class MyPipelines {
    @Produces
    @Singleton
    public Pipeline<?> myPipeline() {
        return Pipeline.of("my-pipeline").build();
    }
}
```

## Configuration

You can configure the admin interface using properties in your `application.properties`:

```properties
data-pipeline.admin.enabled=true
data-pipeline.admin.include=p1,p2
data-pipeline.admin.exclude=p3
```

| Property                      | Description                              | Default           |
|-------------------------------|------------------------------------------|-------------------|
| `data-pipeline.admin.enabled` | Enables or disables the admin interface. | `true`            |
| `data-pipeline.admin.include` | List of pipeline IDs to include.         |                   |
| `data-pipeline.admin.exclude` | List of pipeline IDs to exclude.         |                   |

> ⚠️ Due to limitations in RESTEasy Classic, the admin path is currently hardcoded to `/pipeline-admin` in the Quarkus implementation and cannot be overridden.
