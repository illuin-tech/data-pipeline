# Core Admin

The core admin implementation provides a standalone HTTP server that can be embedded in any Java application.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tech.illuin</groupId>
    <artifactId>data-pipeline-admin</artifactId>
    <version>0.32</version>
</dependency>
```

## Usage

To use the core admin, you can use the `PipelineAdminBuilder` to configure and build a `PipelineAdmin` instance.

```java
Pipeline<?> pipeline;

try (PipelineAdmin admin = new PipelineAdminBuilder().addPipeline(pipeline).build().start())
{
    // The admin is now running on http://localhost:8080/pipeline-admin
    // If you want to keep the application running:
    admin.await();
    // The admin is AutoCloseable and will be automatically stopped when the application exits.
}
```

## Configuration

The `PipelineAdminBuilder` offers several configuration options:

| Method                                              | Description                                                         | Default   |
|-----------------------------------------------------|---------------------------------------------------------------------|-----------|
| `setHost(String)`                                   | Sets the host the server will bind to.                              | `0.0.0.0` |
| `setPort(int)`                                      | Sets the port the server will bind to.                              | `8080`    |
| `addPipeline(Pipeline...)`                          | Adds one or more pipelines to be monitored.                         |           |
| `addPipeline(Collection<Pipeline>)`                 | Adds one or more pipelines to be monitored.                         |           |
| `addPipelineProvider(PipelineProvider...)`          | Adds one or more providers for dynamic pipeline discovery.          |           |
| `addPipelineProvider(Collection<PipelineProvider>)` | Adds one or more providers for dynamic pipeline discovery.          |           |
| `include(String...)`                                | Limits the monitored pipelines to the ones with the specified IDs.  |           |
| `exclude(String...)`                                | Excludes the pipelines with the specified IDs from being monitored. |           |
