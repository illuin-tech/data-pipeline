# Steps

`Step` functions are the place where most business logic happens. 
They are expected to have no side effect and only concern themselves of what their return value will be.

A `Step` is typically a class with a `public` method annotated with the `@StepConfig` annotation, and that method is expected to return a `Result` subtype.
Beside these properties, it can be pretty much anything you want.

In the `README` demonstration pipeline, we wrote the following:

```java
public class Tokenizer
{
    @StepConfig(id = "tokenizer")
    public TokenizedSentence tokenize(@Input String sentence)
    {
        return new TokenizedSentence(Stream.of(sentence.split("[^\\p{L}]+"))
            .map(String::toLowerCase)
            .toList()
        );
    }

    public record TokenizedSentence(
        List<String> tokens
    ) implements Result {}
}
```

This `Step` has an entrypoint, the `tokenize` method is annotated with the `@StepConfig`.
The `id` argument is optional, but if present it will be used in [tags](#componenttag), [metrics](integrations.md#micrometer), [logging](integrations.md#logback-loki), pretty much everywhere the step's identity is concerned.
The `@StepConfig` annotation accepts a few more optional parameters which we dive into in the ["Configuration" section](#configuration). 

It has an `@Input` argument that will be mapped to the pipeline's input.
You can add as many arguments as you need, check out available options in the ["Possible Inputs" section](#possible-inputs).

It returns a `TokenizedSentence` which type is an inner record, there is no single way to manage `Result` subtypes, but it's important to keep in mind that with `data-pipeline`'s [result data model](result_data_model.md) these types are used [for addressing](#by-type).
Here, the inner type is fine if it is closely tied to the `Step` logic, so it can make sense to simply declare the type alongside the `Step`.
In other circumstances, you might need a common result type being its own thing.
Together with a [`ResultEvaluator`](#result-evaluators) they can pull a lot of modeling weight.

## Configuration

`Step` functions can be supplied to a pipeline builder "as-is", meaning you simply `registerStep` the step instance itself:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(new Tokenizer())
    /* ...and others */
    .build()
;
```

This is fine for simple setups and already gets you some of the `data-pipeline` feature-set (component architecture, various integrations, pipeline-level default behaviours, etc.).
But at some point you will want finer-grained configuration at the component level, and this is where the `StepAssembler` steps in.

The `StepAssembler` accepts a `Step` (the one you would be providing directly to the pipeline) and offers you a way to plug in as many modifiers as you need:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(builder -> builder
        .step(new Tokenizer())
        .withId("special-tokenizer")
        .withWrapper(new RetryWrapper<>(RetryConfig.custom().maxAttempts(3).build()))
        .withErrorHandler(mySpecialErrorHandler)
    )
    /* ...and others */
    .build()
;
```

In the snippet above, we added a [retry](integrations.md#retry) and an [error handler](modifiers_and_hooks.md#error-handlers) on the tokenizer.
This is the kind of thing you could be doing if your step has to interact with an unreliable database or external API, for instance.

As we will show in the following sections, some of these options can be set through the `@StepConfig` annotation. It should be noted that the `StepAssembler` options have precedence over the `@StepConfig`, so the latter is a good place to put your step defaults for instance.

## Function Modifiers

The `StepAssembler` accepts a variety of [function modifiers](modifiers_and_hooks.md) which will alter how the `Step` is executed, or how its output (whether on the successful or error path) is handled. 
All of these are optional, but can be very useful in implementing more sophisticated patterns.

### Conditions

The `StepCondition` is a predicate which role is to determine whether the `Step` will be executed based on two types of information:
* the `Context`, most likely for its metadata (see [the relevant documentation](pipelines.md#context)) 
* the `Indexable` being considered for execution, this makes it possible to partition behaviour between different `Indexer` outputs (see the [Initializers documentation](initializers.md#indexers) for more on that)

In the example below, we leverage the generic `MetadataCondition` implementation for performing a check on the `"tokenizer"` key:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(builder -> builder
        .step(new Tokenizer())
        .withCondition(new MetadataCondition("tokenizer", BASIC))
    )
    .registerStep(builder -> builder
        .step(new SuperDuperTokenizer())
        .withCondition(new MetadataCondition("tokenizer", SUPER_DUPER))
    )
    /* ...and others */
    .build()
;

pipeline.run("This is a sentence.", ctx -> ctx.set("tokenizer", BASIC)); // this would match step 1
pipeline.run("This is a sentence.", ctx -> ctx.set("tokenizer", SUPER_DUPER)); // this would match step 2
```

A condition can also be applied via the `@StepConfig` annotation if the `StepCondition` has a default constructor:

```java
@StepConfig(condition = MyCondition.class)
public MyResult doStuff() { /**/ }
```

...or, you can use the default type-based condition for filtering objects ([in the @Object sense](initializers.md#indexers)) of a given type:  

```java
/* This step will only run when the execution object is a MyType subclass */
@StepConfig(conditionOnClass = MyType.class)
public MyResult doStuff() { /**/ }
```

### Error Handlers

The `StepErrorHandler` is a wrapper which role is to act on exceptions thrown by the `Step` it is applied to.
There are two ways `StepErrorHandler` are typically used:
* as exception wrappers: their contract gives access to the original `Step` exception, you can wrap the exception in order to standardize their signature, or introduce an exception type that can encapsulate metadata
* as error recovery procedures: handlers can return a `Result`, this can be leveraged for running fallback code or convert an exception into an "error result", for instance if you want non-blocking errors  

💡 Error handlers also have a [dedicated documentation section](modifiers_and_hooks.md#error-handlers).

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(builder -> builder
        .step(new Tokenizer())
        .withErrorHandler((ex, in, payload, results, ctx) -> new ErrorResult("This didn't go as planned: " + ex.getMessage(), ex))
    )
    /* If we register other steps after the above, they will get executed */
    .build()
;
```

We could imagine gathering all triggered errors for instance, within a `Sink`:

```java
public class ErrorGatherer
{
    @SinkConfig
    public void gatherErrors(@Current Stream<ErrorResult> errors)
    {
        errors.forEach(error -> System.out.println(error.message()));
    }
}
```

An error handler can be applied via the `@StepConfig` annotation if the `StepErrorHandler` has a default constructor:

```java
@StepConfig(errorHandler = MyErrorHandler.class)
public MyResult doStuff() { /**/ }
```

### Result Evaluators

A `ResultEvaluator` is a function that takes the step's return value as input, and can then inform the `Pipeline` about what to do next through a `StepStrategy`.

At the time of this writing a `StepStrategy` can be one of the following ; with `CONTINUE` being the default success strategy, and `EXIT` being the default exception strategy. 

| strategy                    | register result | retain object | run remaining steps | run pinned steps | run sinks |
|-----------------------------|-----------------|---------------|---------------------|------------------|-----------|
| `CONTINUE`                  | yes             | yes           | yes                 | yes              | yes       |
| `SKIP`                      | no              | yes           | yes                 | yes              | yes       |
| `DISCARD_AND_CONTINUE`      | yes             | no            | yes                 | yes              | yes       |
| `STOP`                      | yes             | -             | no                  | yes              | yes       |
| `ABORT`                     | yes             | -             | no                  | no               | yes       |
| `EXIT` (or exception throw) | yes             | -             | no                  | no               | no        |

For instance, if we want a `ResultEvaluator` that will stop the pipeline as soon as a specific `Result` is found:

```java
public class MyResultEvaluator implements ResultEvaluator
{
    @Override
    public StepStrategy evaluate(Result result, Indexable object, Object input, Context<?> ctx)
    {
        if (result instanceof MyStopConditionResult)
            return StepStrategy.STOP;
        return StepStrategy.CONTINUE;
    }
}
```

From there, we would likely apply it as the default evaluator on the whole pipeline:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .setDefaultEvaluator(new MyResultEvaluator())
    /* ...and others */
    .build()
;
```

...or on a single `Step`:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(builder -> builder
        .step(new Tokenizer())
        .withEvaluator(new MyResultEvaluator())
    )
    /* ...and others */
    .build()
;
```

A result evaluator can be applied via the `@StepConfig` annotation if the `ResultEvaluator` has a default constructor:

```java
@StepConfig(evaluator = MyResultEvaluator.class)
public MyResult doStuff() { /**/ }
```

### Wrappers

A `StepWrapper` is a function that takes a `Step` as input and returns a `Step` as output.
The main use for wrappers is to apply generic policies on your business logic, one such example is resilience patterns such as a [retry](integrations.md#retry) or [circuit-breaker](integrations.md#circuitbreaker).

💡 Wrappers also have a [dedicated documentation section](modifiers_and_hooks.md#wrappers).

A simple wrapper implementation can look like this:

```java
public static class MyWrapper<T extends Indexable, I, P> implements StepWrapper<T, I, P>
{
    @Override
    public Step<T, I, P> wrap(Step<T, I, P> step)
    {
        return (obj, in, payload, res, ctx) -> {
            // do stuff
            return step.execute(obj, in, payload, res, ctx);
        };
    }
}
```

Then, the wrapper can be applied as follows:

```java
Pipeline<String, ?> pipeline = Pipeline.<String>of("string-processor")
    .registerStep(builder -> builder
        .step(new Tokenizer())
        .withWrapper(new MyWrapper<>())
    )
    /* ...and others */
    .build()
;
```

## Possible Inputs

`Step` functions accept a variety of inputs, which can be combined as needed.
Some will be mapped by type directly as they are related to `Pipeline` internals (e.g. `Results` or `Context`), others will need additional semantics via annotations.

### `@Input`

When an argument is annotated with `@Input`, the pipeline will attempt to map it to its own input:

```java
@StepConfig
public MyResult doStuff(@Input SomeType in) { /**/ }
```

If the requested input type do not match with the pipeline's input type, an `IllegalArgumentException` will be thrown at execution time.

### `@Current`

When an argument is annotated with `@Current`, the pipeline will attempt to map it to a ["current result"](result_data_model.md#result-container) in the `ResultContainer`.

#### By type

If the argument annotated is a `Result` subtype, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified type.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "current result" can be found for that type, a `NoSuchElementException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Current SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@StepConfig
public MyResult doStuff(@Current Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that result type */
@StepConfig
public MyResult doStuff(@Current Stream<SomeResult> results) { /**/ }
```

#### By name

If the argument annotated has a specified name, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified name.
This can be most helpful in cases where you want to standardize `Result` types and reuse a single type for different semantics. 

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "current result" can be found for that name, a `NoSuchElementException` will be thrown at execution time.

If a "current result" can be found for that name, but the match has a type mismatch, an `IllegalArgumentException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Current(name = "my_name") SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@StepConfig
public MyResult doStuff(@Current(name = "my_name") Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@StepConfig
public MyResult doStuff(@Current(name = "my_name") Stream<SomeResult> results) { /**/ }
```

### `@Latest`

When an argument is annotated with `@Latest`, the pipeline will attempt to map it to a ["latest result"](result_data_model.md#result-container) in the `ResultContainer`.

#### By type

If the argument annotated is a `Result` subtype, the pipeline will attempt to recover the latest "latest result" from the `ResultContainer` matching the specified type.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "latest result" can be found for that type, a `NoSuchElementException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Latest SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@StepConfig
public MyResult doStuff(@Latest Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@StepConfig
public MyResult doStuff(@Latest Stream<SomeResult> results) { /**/ }
```

#### By name

If the argument annotated has a specified name, the pipeline will attempt to recover the latest "current result" from the `ResultContainer` matching the specified name.
This can be most helpful in cases where you want to standardize `Result` types and reuse a single type for different semantics.

If the annotation is put on a non-Result argument type, the pipeline will throw an `IllegalStateException` at build time.

If no "latest result" can be found for that name, a `NoSuchElementException` will be thrown at execution time.

If a "latest result" can be found for that name, but the match has a type mismatch, an `IllegalArgumentException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Latest(name = "my_name") SomeResult result) { /**/ }

/* The argument can be an `Optional`, it will be empty if no match can be found */
@StepConfig
public MyResult doStuff(@Latest(name = "my_name") Optional<SomeResult> result) { /**/ }

/* The argument can be a `Stream`, in will contain all matches for that name */
@StepConfig
public MyResult doStuff(@Latest(name = "my_name") Stream<SomeResult> results) { /**/ }
```

### `Results` and `ResultView`

The `Results` and `ResultView` arguments give you access to the whole `ResultContainer`, 💡 more information on their respective feature sets in the ["Result Data Model" section](result_data_model.md#result-container).

They are mapped by type so no specific annotation is required:

```java
@StepConfig
public MyResult doStuff(Results results)
{
    //results.current(SomeResult.class).orElseThrow();
}
```

### `@Payload`

The pipeline's payload can be passed as argument with the `@Payload` annotation, 💡 more information on payloads [in the initializer section](initializers.md).

If the requested and actual types do not match, an `IllegalArgumentException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Payload MyPayload payload) { /**/ }
```

### `@Object`

The current object can be passed as argument with the `@Object` annotation, 💡 more information on objects [in the initializer section](initializers.md#indexers).

If the requested and actual types do not match, an `IllegalArgumentException` will be thrown at execution time.

```java
@StepConfig
public MyResult doStuff(@Object SomeIndexable object) { /**/ }
```

### `PipelineTag`

The `PipelineTag` can be passed as argument, they are mapped by type so no specific annotation is required.

Pipeline tags are generated at the very start of the pipeline and contain the following properties:
* a `uid` as generated [by the `UIDGenerator`](modifiers_and_hooks.md#uid-generators)
* a `pipeline` name as defined [by its configuration](pipelines.md#configuration)
* an `author` name as extracted [by the `AuthorResolver`](modifiers_and_hooks.md#author-resolvers)

```java
@StepConfig(id = "my-step")
public MyResult doStuff(PipelineTag tag)
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
@StepConfig(id = "my-step")
public MyResult doStuff(ComponentTag tag)
{
    /*
     * The following would print something along the lines of:
     * ComponentTag[uid=2zongm8nvgu7jrlc5tl0tbgcexk, pipelineTag=PipelineTag[uid=2zongloalw6vwnbrs7joqgf0cxh, pipeline=my-pipeline, author=anonymous], id=my-step, family=STEP]
     */
    System.out.println(tag);
}
```

### `Context<P>`

The `Context` can be mapped by type, and gives you access to the pipeline's context:

```java
@StepConfig
public MyResult doStuff(Context<?> context)
{
    //context.get("my_metadata_key", SomeType.class).orElseThrow();
}
```

💡 More info on the context [in the pipeline's section](pipelines.md#context).

### `UIDGenerator`

You can access the `UIDGenerator` [currently in use by the pipeline](modifiers_and_hooks.md#uid-generators) by requesting it as an argument:

```java
@StepConfig
public MyResult doStuff(UIDGenerator generator)
{
    //generator.generate();
}
```

This can be useful if you want to harmonize the generation of UIDs between `data-pipeline` managed data and more business-centric data.

Typically, if data-lineage is a concern, you might want to persist the [`PipelineTag`](#pipelinetag) or [`ComponentTag`](#componenttag) in some data store.
It may then be relevant to use the same UID generation strategy for other data models, as these UIDs can have properties such as being [time-stamped](https://github.com/twitter-archive/snowflake/tree/snowflake-2010) or [lexicographically sortable](https://github.com/ulid/spec).

### `LogMarker`

The `LogMarker` is a component that can produce a `LabelMarker` out of the `TagResolver` currently in use by the pipeline. 

Its use is encouraged for annotating your own logs with contextual information (💡 see [the relevant `TagResolver` section](modifiers_and_hooks.md#tag-resolvers)).

```java
@StepConfig
public MyResult doStuff(LogMarker marker)
{
    logger.info(marker.mark(), "This is my log: {}", 123);
    logger.info(marker.mark("my_local_key", "my_value"), "This is my log: {}", 234);
}
```

## Results

All step functions' `Result` are tracked by the pipeline, and can be used by subsequent steps, sinks, or returned as part of [the final `Output`](pipelines.md#output).

The `Result` interface is a very simple contract with a single `name()` method, which has a default implementation simply returning the class name.
The value returned by `name()` can be used for addressing results within the `ResultContainer`, 💡 more information on that [in the dedicated section](result_data_model.md).
