# Sinks

`Sink` functions are the place where your pipeline interfaces with the outside world.
They are expected to use parts (or all) of the step results and impact external services: database, message queues, APIs, etc.

A `Sink` is implemented as a class with a `public` method annotated with the `@SinkConfig` annotation, that method is expected to have a `void` return type.
Beside these properties, it can be pretty much anything you want.

A typical example of a `Sink` could look like this:

```java
public class MessagePublisher
{
    private final String topic;
    private final Producer<String, SomeRecordData> producer;
    
    public MessagePublisher(String topic, Producer<String, SomeRecordData> producer)
    {
        this.topic = topic;
        this.producer = producer;
    }

    @SinkConfig(id = "message-publisher")
    public void produce(@Current SomeResult result)
    {
        var record = new ProducerRecord<>(this.topic, map(result));
        this.producer.produce(record);
    }
    
    private static SomeRecordData map(SomeResult result)
    {
        return SomeRecordData.newBuilder()
            //.setSomeValue(result.getSomeValue())
            .build()
        ;
    }
}
```

## Configuration

Much the same way as steps, `Sink` functions can be supplied to a pipeline builder "as-is", meaning you simply `registerSink` over the sink instance itself:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(new Tokenizer())
    /* ...and others */
    .build()
;
```

For finer-grained configuration, the `SinkAssembler` accepts a `Sink` (the one you would be providing directly to the pipeline) and offers you a way to plug in as many modifiers as you need:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerSink(builder -> builder
        .sink(new MessagePublisher(topicName, producer))
        .withId("special-publisher")
        .withWrapper(new RetryWrapper<>(RetryConfig.custom().maxAttempts(3).build()))
        .withErrorHandler(mySpecialErrorHandler)
    )
    /* ...and others */
    .build()
;
```

In the snippet above, we added a [retry](integrations.md#retry) and an [error handler](modifiers_and_hooks.md#error-handlers) on the publisher.
This is the kind of thing you could be doing if your sink has to interact with an unreliable database or external API, for instance.

As we will show in the following sections, some of these options can be set through the `@SinkConfig` annotation. It should be noted that the `SinkAssembler` options have precedence over the `@SinkConfig`, so the latter is a good place to put your step defaults for instance.

### Parallel Asynchronous Sinks

By default `Sink` functions are executed serially and synchronously with the `Pipeline`, the order in which they are executed is the order in which they were registered (same as steps).

But, for some situations (e.g. long blocking calls) it can be handy to offload the `Sink` execution in order to increase the pipeline throughput.
This makes it possible to run part (or all) of the sinks in a parallel asynchronous fashion.

The way it is currently handled in `data-pipeline` is by allowing the use of a `ServiceExecutor` (by default a `ThreadPoolExecutor` with as many threads as `Runtime.getRuntime().availableProcessors()`).
The `ServiceExecutor` to be used can be customized with the pipeline builder, either directly

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .setSinkExecutor(myExecutor)
    .build()
;
```

...or via a supplier:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .setSinkExecutorProvider(() -> Executors.newFixedThreadPool(32))
    .build()
;
```

The `Pipeline` sink phase will segregate synchronous and asynchronous sinks based on the `async` property, which can be set either via the `@SinkConfig` annotation:

```java
@SinkConfig(id = "message-publisher", async = true)
public void produce(@Current SomeResult result) { /**/ }
```

...or via the `SinkAssembler`:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerSink(builder -> builder
        .sink(new MessagePublisher(topicName, producer))
        .setAsync(true)
    )
    /* ...and others */
    .build()
;
```

## Function Modifiers

The `SinkAssembler` accepts a variety of [function modifiers](modifiers_and_hooks.md) which will alter how the `Sink` is executed.
All of these are optional, but can be very useful in implementing more sophisticated patterns.

### Error Handlers

The `SinkErrorHandler` is a wrapper which role is to act on exceptions thrown by the `Sink` it is applied to.
These handlers are useful:
* as exception wrappers: their contract gives access to the original `Sink` exception, you can wrap the exception in order to standardize their signature, or introduce an exception type that can encapsulate metadata
* as error recovery procedures: they can be leveraged for running fallback code

Error handlers also have a [dedicated documentation section](modifiers_and_hooks.md#error-handlers).

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerSink(builder -> builder
        .sink(new MessagePublisher(topicName, producer))
        .withErrorHandler((ex, out, ctx) -> logger.error("Error: {}", ex.getMessage()))
    )
    /* If we register other sinks after the above, they will get executed */
    .build()
;
```

An error handler can be applied via the `@SinkConfig` annotation if the `SinkErrorHandler` has a default constructor:

```java
@SinkConfig(errorHandler = MyErrorHandler.class)
public void doStuff() { /**/ }
```

### Wrappers

A `SinkWrapper` is a function that takes a `Sink` as input and returns a `Sink` as output.
The main use for wrappers is to apply generic policies on your business logic, one such example is resilience patterns such as a [retry](integrations.md#retry) or [circuit-breaker](integrations.md#circuitbreaker).

Wrappers also have a [dedicated documentation section](modifiers_and_hooks.md#wrappers).

A simple wrapper implementation can look like this:

```java
public static class MyWrapper<P> implements SinkWrapper<P>
{
    @Override
    public Sink<P> wrap(Sink<P> sink)
    {
        return (out, ctx) -> {
            // do stuff
            sink.execute(out, ctx);
        };
    }
}
```

Then, the wrapper can be applied as follows:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerSink(builder -> builder
        .sink(new MessagePublisher(topicName, producer))
        .withWrapper(new MyWrapper<>())
    )
    /* ...and others */
    .build()
;
```

## Possible Inputs

`Sink` functions accept a variety of inputs, which can be combined as needed.
Some will be mapped by type directly as they are related to `Pipeline` internals (e.g. `Results` or `Context`), others will need additional semantics via annotations.

### `@Input`

When an argument is annotated with `@Input`, the pipeline will attempt to map it to its own input:

```java
@SinkConfig
public void doStuff(@Input SomeType in) { /**/ }
```

If the requested input type do not match with the pipeline's input type, an `IllegalArgumentException` will be thrown at execution time.

### `@Current`

When an argument is annotated with `@Current`, the pipeline will attempt to map it to a ["current result"](result_data_model.md#result-container) in the `ResultContainer`.

#### By type

If the argument annotated is a `Result` subtype, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified type.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "current result" can be found for that type, a `NoSuchElementException` will be thrown at execution time.

```java
@SinkConfig
public void doStuff(@Current SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@SinkConfig
public void doStuff(@Current Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that result type */
@SinkConfig
public void doStuff(@Current Stream<SomeResult> results) { /**/ }
```

#### By name

If the argument annotated has a specified name, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified name.
This can be most helpful in cases where you want to standardize `Result` types and reuse a single type for different semantics.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "current result" can be found for that name, a `NoSuchElementException` will be thrown at execution time.

If a "current result" can be found for that name, but the match has a type mismatch, an `IllegalArgumentException` will be thrown at execution time.

```java
@SinkConfig
public void doStuff(@Current(name = "my_name") SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@SinkConfig
public void doStuff(@Current(name = "my_name") Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@SinkConfig
public void doStuff(@Current(name = "my_name") Stream<SomeResult> results) { /**/ }
```

### `@Latest`

When an argument is annotated with `@Latest`, the pipeline will attempt to map it to a ["latest result"](result_data_model.md#result-container) in the `ResultContainer`.

#### By type

If the argument annotated is a `Result` subtype, the pipeline will attempt to recover the latest "latest result" from the `ResultContainer` matching the specified type.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "latest result" can be found for that type, a `NoSuchElementException` will be thrown at execution time.

```java
@SinkConfig
public void doStuff(@Latest SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@SinkConfig
public void doStuff(@Latest Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@SinkConfig
public void doStuff(@Latest Stream<SomeResult> results) { /**/ }
```

#### By name

If the argument annotated has a specified name, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified name.
This can be most helpful in cases where you want to standardize `Result` types and reuse a single type for different semantics.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "latest result" can be found for that name, a `NoSuchElementException` will be thrown at execution time.

If a "latest result" can be found for that name, but the match has a type mismatch, an `IllegalArgumentException` will be thrown at execution time.

```java
@SinkConfig
public void doStuff(@Latest(name = "my_name") SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@SinkConfig
public void doStuff(@Latest(name = "my_name") Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@SinkConfig
public void doStuff(@Latest(name = "my_name") Stream<SomeResult> results) { /**/ }
```

### `Results`

The `Results` argument gives you access to the whole `ResultContainer`, more information on its feature set in the ["Result Data Model" section](result_data_model.md#result-container).

It is mapped by type so no specific annotation is required:

```java
@SinkConfig
public void doStuff(Results results)
{
    //results.current(SomeResult.class).orElseThrow();
}
```

### `@Payload`

The pipeline's payload can be passed as argument with the `@Payload` annotation, more information on payloads [in the initializer section](initializers.md).

If the requested and actual types do not match, an `IllegalArgumentException` will be thrown at execution time.

```java
@SinkConfig
public void doStuff(@Payload MyPayload payload) { /**/ }
```

### `PipelineTag`

The `PipelineTag` can be passed as argument, they are mapped by type so no specific annotation is required.

Pipeline tags are generated at the very start of the pipeline and contain the following properties:
* a `uid` as generated [by the `UIDGenerator`](modifiers_and_hooks.md#uid-generators)
* a `pipeline` name as defined [by its configuration](pipelines.md#configuration)
* an `author` name as extracted [by the `AuthorResolver`](modifiers_and_hooks.md#author-resolvers)

```java
@SinkConfig(id = "my-sink")
public void doStuff(PipelineTag tag)
{
    /*
     * The following would print something along the lines of:
     * PipelineTag[uid=2zongloalw6vwnbrs7joqgf0cxh, pipeline=my-pipeline, author=anonymous]
     */
    System.out.println(tag);
}
```

### `ComponentTag`

The `ComponentTag` can be passed as argument, they are mapped by type so no specific annotation is required.

Component tags are generated at the start of each component run and contain the following properties:
* a `uid` as generated [by the `UIDGenerator`](modifiers_and_hooks.md#uid-generators)
* an `id` name as defined [by its configuration](#configuration)
* a `family` name depending on the type of component (`INITIALIZER`, `STEP` or `SINK`)
* a `pipelineTag` reference to [current pipeline's `PipelineTag`](#pipelinetag)

```java
@SinkConfig(id = "my-sink")
public void doStuff(ComponentTag tag)
{
    /*
     * The following would print something along the lines of:
     * ComponentTag[uid=2zongm8nvgu7jrlc5tl0tbgcexk, pipelineTag=PipelineTag[uid=2zongloalw6vwnbrs7joqgf0cxh, pipeline=my-pipeline, author=anonymous], id=my-sink, family=SINK]
     */
    System.out.println(tag);
}
```

### `Output<P>`

The `Output` can be mapped by type, it gives you access to the pipeline run's output as it is right after all step functions are executed.

```java
@SinkConfig
public void doStuff(Output<?> output)
{
    //output.tag()
    //output.payload()
    //output.results()
}
```

This is a bit of a catch-all argument, so definitely not a first-pick, but it can be relevant especially [when combined to a custom `OutputFactory`](pipelines.md#output) for certain use-cases.

### `Context<P>`

The `Context` can be mapped by type, it gives you access to the pipeline's context:

```java
@SinkConfig
public void doStuff(Context<?> context)
{
    //context.get("my_metadata_key", SomeType.class).orElseThrow();
}
```

More info on the context [in the pipeline's section](pipelines.md#context).

### `UIDGenerator`

You can access the `UIDGenerator` [currently in use by the pipeline](modifiers_and_hooks.md#uid-generators) by requesting it as an argument:

```java
@SinkConfig
public void doStuff(UIDGenerator generator)
{
    //generator.generate();
}
```

This can be useful if you want to harmonize the generation of UIDs between `data-pipeline` managed data and more business-centric data.

Typically, if data-lineage is a concern, you might want to persist the [`PipelineTag`](#pipelinetag) or [`ComponentTag`](#componenttag) in some data store.
It may then be relevant to use the same UID generation strategy for other data models, as these UIDs can have properties such as being [time-stamped](https://github.com/twitter-archive/snowflake/tree/snowflake-2010) or [lexicographically sortable](https://github.com/ulid/spec).

### `LogMarker`

The `LogMarker` is a component that can produce a `LabelMarker` out of the `TagResolver` currently in use by the pipeline.

Its use is encouraged for annotating your own logs with contextual information (see [the relevant `TagResolver` section](modifiers_and_hooks.md#tag-resolvers)).

```java
@SinkConfig
public void doStuff(LogMarker marker)
{
    logger.info(marker.mark(), "This is my log: {}", 123);
    logger.info(marker.mark("my_local_key", "my_value"), "This is my log: {}", 234);
}
```
