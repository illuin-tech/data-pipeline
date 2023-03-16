package tech.illuin.pipeline.generic;

import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.generic.model.A;
import tech.illuin.pipeline.generic.model.B;
import tech.illuin.pipeline.generic.pipeline.TestResult;
import tech.illuin.pipeline.generic.pipeline.step.TestAnnotatedSteps;
import tech.illuin.pipeline.generic.pipeline.step.TestStep;
import tech.illuin.pipeline.input.indexer.SingleIndexer;
import tech.illuin.pipeline.step.execution.evaluator.ResultEvaluator;
import tech.illuin.pipeline.step.execution.wrapper.retry.RetryWrapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.illuin.pipeline.step.execution.evaluator.StepStrategy.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestFactory
{
    private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

    private static A initializer(Void input, Context<A> context)
    {
        return new A(List.of(
            new B("b1"),
            new B("b2")
        ));
    }

    private static A initializerOfEmpty(Void input, Context<A> context)
    {
        return new A(Collections.emptyList());
    }

    public static Pipeline<Void, A> createAbortingPipeline()
    {
        return Pipeline.ofPayload("test-abort", TestFactory::initializer)
            .registerIndexer(SingleIndexer.auto())
            .registerIndexer(A::bs)
            .registerStep(builder -> builder
                .step(new TestStep<>("1", "ok"))
                .withCondition(A.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<B>("2", b -> b.name().equals("b1") ? "ok" : "ko"))
                .withEvaluation(res -> ((TestResult) res).status().equals("ok") ? CONTINUE : DISCARD_AND_CONTINUE)
                .withCondition(B.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("3", "ok"))
                .withCondition(B.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("4", "ko"))
                .withEvaluation(res -> ABORT)
                .withCondition(A.class)
            )
            .registerStep(new TestStep<>("5", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void, A> createErrorHandledPipeline()
    {
        return Pipeline.ofPayload("test-error-handled", TestFactory::initializer)
            .registerIndexer(SingleIndexer.auto())
            .registerIndexer(A::bs)
            .registerStep(builder -> builder
                .step(new TestStep<>("1", "ok"))
                .withCondition(A.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("2", "ok"))
                .withCondition(B.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("3", b -> { throw new RuntimeException("Some error"); }))
                .withErrorHandler((ex, ctx) -> new TestResult("error", "ko"))
                .withEvaluation(ResultEvaluator.ALWAYS_ABORT)
                .withCondition(A.class)
            )
            .registerStep(new TestStep<>("4", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void, A> createErrorHandledWithPinnedPipeline(ResultEvaluator evaluator)
    {
        return Pipeline.ofPayload("test-error-handled-with-pinned", TestFactory::initializer)
           .registerIndexer(SingleIndexer.auto())
           .registerIndexer(A::bs)
           .registerStep(builder -> builder
               .step(new TestStep<>("1", "ok"))
               .withId("step-1")
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("2", b -> { throw new RuntimeException("Some error"); }))
               .withId("step-2")
               .withErrorHandler((ex, ctx) -> new TestResult("error", "ko"))
               .withEvaluation(evaluator)
               .withCondition(A.class)
           )
           .registerStep(builder -> builder
               .step(new TestStep<>("3", "ok"))
               .withId("step-3")
               .withCondition(A.class)
               .setPinned(true)
           )
           .build()
        ;
    }

    public static Pipeline<Void, A> createErrorThrownPipeline()
    {
        return Pipeline.ofPayload("test-error-thrown", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(builder -> builder
                .step(new TestStep<>("1", "ok"))
                .withCondition(A.class)
            )
            .registerStep(builder -> builder
                .step(new TestStep<>("2", b -> { throw new RuntimeException("Some error"); }))
                .withCondition(A.class)
            )
            .registerStep(new TestStep<>("3", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void, A> createErrorRetryPipeline()
    {
        var counter = new AtomicInteger(0);
        return Pipeline.ofPayload("test-error-retry", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestStep<>("1", "ok"))
            .registerStep(builder -> builder
                .step(new TestStep<>("2", b -> {
                    if (counter.getAndIncrement() < 4)
                        throw new RuntimeException("Some error");
                    return "ok";
                }))
                .withWrapper(new RetryWrapper<>(RetryConfig.custom()
                    .maxAttempts(5)
                    .waitDuration(Duration.ofMillis(500))
                    .build()
                ))
            )
            .registerStep(new TestStep<>("3", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void, A> createAnnotatedPipeline()
    {
        return Pipeline.ofPayload("test-annotated", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestAnnotatedSteps.ClassActivation<>("1", "ok"))
            .registerStep(new TestAnnotatedSteps.ConditionActivation<>("2", "ok"))
            .registerStep(new TestAnnotatedSteps.ErrorHandler<>("3", b -> { throw new RuntimeException("Some error"); }))
            .registerStep(new TestAnnotatedSteps.ClassActivation<>("4", "ok"))
            .registerStep(new TestAnnotatedSteps.Pinned<>("5", "ok"))
            .build()
        ;
    }

    public static Pipeline<Void, A> createPipelineWithSinks(AtomicInteger counter)
    {
        return Pipeline.ofPayload("test-annotated", TestFactory::initializerOfEmpty)
            .registerIndexer(SingleIndexer.auto())
            .registerStep(new TestAnnotatedSteps.ClassActivation<>("1", "ok"))
            .registerStep(new TestAnnotatedSteps.ConditionActivation<>("2", "ok"))
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .registerSink((o, ctx) -> counter.incrementAndGet())
            .registerSink((o, ctx) -> counter.incrementAndGet(), true)
            .registerSink((o, ctx) -> counter.incrementAndGet(), true)
            .build()
        ;
    }
}
