# Function Modifiers & Hooks

## Error Handlers

Error handlers are for acting on exceptions thrown from within a component, typical use-cases include recovering from the error or wrapping the original exception into another exception type.

There are three error handler types, one for each type of component:
* `InitializerErrorHandler` for initializers, they can produce a payload
* `StepErrorHandler` for steps, they can produce a `Result`
* `SinkErrorHandler` for sinks

An error handler implementation looks something like this:

```java
public class MyHandler implements StepErrorHandler
{
    @Override
    public Result handle(Exception exception, Object input, Object payload, Results results, Context context)
    {
        logger.error("We did get an error.. ({})", exception.getMessage());
        return new MyResult("..but let's ignore it anyway");
    }
}
```

A handy feature of error handlers is that they can be combined with their `andThen` method.

```java
// Assuming a pipeline builder
pipelineBuilder.registerStep(builder -> builder
    .step(someStep)
    .withErrorHandler(someHandler.andThen(otherHandler))
);
```

All error handler interfaces have a no-op implementation named `RETHROW_ALL` which simply rethrows the original exception.
By default, `Pipeline` instances are built with those as default handlers.  

## Pipeline Error Handlers

Pipeline error handlers are responsible for managing exceptions occurring during the pipeline run, before they are thrown by the pipeline.
Their typical use would be for running a recovery pipeline, or wrapping outgoing exceptions before their throw.

An exemple for a recovery pattern could look like this:

```java
/* Given a hypothetical pipeline builder */
PayloadPipelineBuilder<String> builder;
/* Given a hypothetical recovery pipeline with a String input */
Pipeline<String> recovery;

PipelineErrorHandler recoveryHandler = (exception, previous, input, context) -> recovery.run(
    (String) input,
    new SimpleContext(previous).copyFrom(context)
);
builder.setErrorHandler(recoveryHandler);
```

In the example above, if the pipeline resulting from `builder` fails, it will hand over processing to the `recovery` pipeline.
Note that the context inherits from the previous run, so `Result` created by the main pipeline, as well as original context metadata will be available in the recovery pipeline. 

## Wrappers

Wrappers are for encapsulating the execution of pipeline components, they allow the execution of additional logic before and/or after a component is executed.
They can do things as varied as performing retries if a component fails, or ensuring execution rate limitations (💡 notably, see current [`resilience4j` integrations](integrations.md#resilience4j)).

At the time of this writing, only `Step` and `Sink` components can be wrapped.
Due to differences in their respective signatures and role within a `Pipeline`, each have matching wrapper contracts:
* `StepWrapper` for steps
* `SinkWrapper` for sinks

A custom `StepWrapper` may look like the following, it takes the current step as input, and returns the step that should run in its stead:

```java
public class MyWrapper<T, I> implements StepWrapper<T, I>
{
    @Override
    public Step<T, I> wrap(Step<T, I> step)
    {
        return (object, input, payload, results, context) -> {
            System.out.println("This is something I need to print before");
            return step.execute(object, input, payload, results, context);
        };
    }
}
```

Similarly, a `SinkWrapper`:

```java
public class MyWrapper implements SinkWrapper
{
    @Override
    public Sink wrap(Sink sink)
    {
        return (output, context) -> {
            System.out.println("This is something I need to print before");
            sink.execute(output, context);
        };
    }
}
```

Like error handlers, wrappers can be combined with their `andThen` method, and used like such:

```java
// Assuming a pipeline builder
pipelineBuilder.registerStep(builder -> builder
    .step(someStep)
    .withWrapper(someWrapper.andThen(otherWrapper))
);
```

> 💡 There are several implementations of wrappers available out-of-the-box, as documented in the [integrations section](integrations.md#resilience4j).

All wrapper interfaces have a no-op implementation named `noOp` which simply return the original step.
By default, `Pipeline` instances are built with those as default wrappers.

## UID Generators

When a pipeline is run, the better part of its behaviours are tracked and uniquely identified with a UID (💡 see the documentation on [tags](result_data_model.md#tags-and-lineage)).
The `UIDGenerator` is the component responsible for producing a UID within a `Pipeline`, and it can be tailored to your needs if you need a specific one (e.g. a DB persistence of pipeline results may require specific types of UIDs).

`data-pipeline` provides the following strategies out of the box:
* a `ksuid` generator (`KSUIDGenerator`) from [`ksuid-creator`](https://github.com/f4b6a3/ksuid-creator), this is the default implementation
* a `tsid` generator (`TSIDGenerator`) from [`tsid-creator`](https://github.com/f4b6a3/tsid-creator)
* a `ulid` generator (`ULIDGenerator`) from [`ulid-creator`](https://github.com/f4b6a3/ulid-creator)
* a `uuid` generator (`UUIDGenerator`) from `java.util.UUID`

> 💡 More on the subject of UUIDs in Twilio's ["A brief history of the UUID"](https://segment.com/blog/a-brief-history-of-the-uuid/), the GitHub repositories above also have great descriptions of what each implementation can do.
> For instance, you can leverage the `tsid` implementation for generating UIDs similar to [Twitter Snowflakes](https://github.com/twitter-archive/snowflake) or [Discord Snowflakes](https://discord.com/developers/docs/reference#snowflakes).

A custom `UIDGenerator` can look like this:

```java
public class DiscordSnowflakeGenerator implements UIDGenerator
{
    private final TsidFactory factory;
    
    public DiscordSnowflakeGenerator(int worker, int process)
    {
        this.factory = TsidFactory.builder()
            // Discord Epoch starts in the first millisecond of 2015
            .withCustomEpoch(Instant.parse("2015-01-01T00:00:00.000Z"))
            // Discord Snowflakes have 5 bits for worker ID and 5 bits for process ID
            .withNode(worker << 5 | process)
            .build()
        ;
    }
    
    @Override
    public String generate()
    {
        return this.factory.create();
    }
}
```

## Author Resolvers

An `AuthorResolver` is a component that is used upon launching a pipeline for flagging the pipeline execution with an author identifier.
This can be any `String` you want, like a username or entity reference.

A typical `AuthorResolver` may look like this:

```java
public class MyAuthorResolver implements AuthorResolver<MyInputType>
{
    @Override
    public String resolve(MyInputType input, Context context)
    {
        return input.getUsername();
    }
}
```

Authorship is used in pipeline and component tags, respectively produced at each pipeline and component (initializer, step, sink) run, 💡 see [the relevant section for more details on that](result_data_model.md#tags-and-lineage).   

## Tag Resolvers

A `TagResolver` is a component that is used upon launching a pipeline for generating custom tags that will be used in metrics (micrometer / prometheus) and log markers (slf4j / logback / log4j). 

A typical `TagResolver` may look like this:

```java
public class MyTagResolver implements TagResolver<MyInputType>
{
    @Override
    public MetricTags resolve(MyInputType input, Context context)
    {
        return new MetricTags()
            .put("some_tag", input.getSome())
            .put("other_tag", input.getOther())
        ;
    }
}
```

In this case metrics and logs will be annotated with —in addition to their default tags— the `some_tag` and `other_tag` tags.

For instance, in prometheus this may result in the following breakdown:

```
pipeline_run_success_total{other_tag="abc",pipeline="my-pipeline",some_tag="123",} 687613.0
pipeline_run_success_total{other_tag="abc",pipeline="my-pipeline",some_tag="234",} 3278.0
pipeline_run_success_total{other_tag="bcd",pipeline="my-pipeline",some_tag="123",} 142.0
pipeline_run_success_total{other_tag="bcd",pipeline="my-pipeline",some_tag="234",} 1571632.0
```

These same labels will be used for MDC logging and so can be made available as labels in you configuration, for instance with the `Loki4jAppender`:

```xml
<!-- ... -->
<appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
    <http>
        <url>${LOKI_HOST}${LOKI_ENDPOINT}</url>
    </http>
    <format>
        <label>
            <!-- if you add %mdc all tags will be passed as labels -->
            <!-- you can also require specific keys, see https://logback.qos.ch/manual/layouts.html#mdc for more info -->
            <pattern>app=my-app,%level,%mdc</pattern>
            <readMarkers>true</readMarkers>
        </label>
        <message>
            <pattern>l=%level c=%logger{20} t=%thread | %msg %ex</pattern>
        </message>
        <sortByTime>true</sortByTime>
    </format>
</appender>
<!-- ... -->
```

Then the following will be available in Loki:

```
Log labels
  level     INFO
  app       my-app
  pipeline  my-pipeline
  author    anonymous
  some_tag  123
  other_tag abc
```
