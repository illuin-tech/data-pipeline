# Result Data Model

In this section, we will explain how return values from [steps](steps.md) are organized and the different ways you can access them in a pipeline.

The core principle to keep in mind is that in `data-pipeline`, there are three ways data is expected to flow:
* inbound from the outside: the pipeline's input, considered immutable
* within the pipeline: step results, considered immutable 
* outbound from the pipeline: sinks with side effects, or eventually via the `Output` container

Each step in a pipeline produces a `Result` which is pushed and tracked into a structure called the `ResultContainer`:

![](resources/result-data-model-step-and-result.png)

Afterward, every next component along the way will have access to previously pushed results.
For instance, if we were to write the `Step C` from above, we could ask for `Result A` and `Result B` to be passed as arguments:

```java
public class StepC
{
    //where A, B and C are Results
    @StepConfig
    public C doStuff(@Current A resultA, @Current B resultB) {}
}
```

Or in a `Sink`, you would have access to all produced results ([assuming all steps were executed and no result was discarded](steps.md#result-evaluators)):

```java
public class MySink
{
    //where A, B and C are Results
    @SinkConfig
    public void doStuff(@Current A resultA, @Current B resultB, @Current C resultC) {}
}
```

## Tags and Lineage



## Result Container

The `ResultContainer` is responsible for holding references to all results produced during a `Pipeline` run.

They come with a variety of methods for accessing results, allowing for different strategies:
* results can be identified by their java type (most common use-case)
* results can be identified by a user-defined name (for discriminating generic result types)
* results can be considered `Optional` if a step execution is optional or if it returns results of varying identifiers
* results can be considered a `Stream` if several steps produce results with the same identifier
* results have a scope: `current` and/or `latest`
  * `current` covers the current execution scope, i.e. only what steps produced since the pipeline started
  * `latest` covers both the current execution scope and results supplied via [pipeline inheritance](#pipeline-inheritance-and-result-continuity)

![](resources/result-data-model-scopes.png)



## Pipeline Inheritance and Result Continuity

![](resources/result-data-model-pipeline-inheritance.png)


