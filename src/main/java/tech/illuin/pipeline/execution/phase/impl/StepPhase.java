package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.PipelineResult;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineStepMetrics;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.Result;
import tech.illuin.pipeline.step.result.ResultDescriptor;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static tech.illuin.pipeline.step.execution.evaluator.StrategyBehaviour.*;
import static tech.illuin.pipeline.step.execution.evaluator.StrategyBehaviour.STOP_ALL;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepPhase<I, P> implements PipelinePhase<I, P>
{
    private final Pipeline<I, P> pipeline;
    private final List<StepDescriptor<Indexable, I, P>> steps;
    private final UIDGenerator uidGenerator;
    private final MeterRegistry meterRegistry;
    
    private static final Logger logger = LoggerFactory.getLogger(StepPhase.class);

    public StepPhase(Pipeline<I, P> pipeline, List<StepDescriptor<Indexable, I, P>> steps, UIDGenerator uidGenerator, MeterRegistry meterRegistry)
    {
        this.pipeline = pipeline;
        this.steps = steps;
        this.uidGenerator = uidGenerator;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(I input, Output<P> output, Context<P> context) throws Exception
    {
        try {
            Set<Indexable> discarded = new HashSet<>();
            STEP_LOOP: for (StepDescriptor<Indexable, I, P> step : this.steps)
            {
                String name = getPrintableName(step);
                PipelineStepMetrics metrics = new PipelineStepMetrics(this.meterRegistry, this.pipeline, step);

                /* Arguments are a list of Indexable which satisfy the step's execution predicate */
                List<Indexable> arguments = output.index().stream()
                    .filter(idx -> !discarded.contains(idx) || step.isPinned())
                    .filter(idx -> step.canExecute(idx, context))
                    .toList()
                ;

                logger.trace(metrics.marker(output.tag()), "{}#{} retrieved {} arguments for step {}", this.pipeline.id(), output.tag().uid(), arguments.size(), name);

                /* For each argument we perform the step and register the produced Result */
                for (Indexable indexed : arguments)
                {
                    Result result = this.runStep(step, indexed, input, output, context, metrics);
                    metrics.resultCounter(result).increment();

                    StepStrategy strategy = step.postEvaluation(result, indexed, input, context);
                    logger.trace(metrics.marker(output.tag()), "{}#{} received {} signal after step {} over argument {}", this.pipeline.id(), output.tag().uid(), strategy, name, indexed.uid());

                    if (strategy.hasBehaviour(REGISTER_RESULT))
                    {
                        if (result instanceof PipelineResult<?> pResult)
                            pResult.output().results().descriptors().current().forEach(rd -> output.results().register(indexed.uid(), rd));
                        else {
                            output.results().register(indexed.uid(), new ResultDescriptor<>(
                                this.uidGenerator.generate(),
                                output.tag(),
                                Instant.now(),
                                result
                            ));
                        }
                    }
                    if (strategy.hasBehaviour(EXIT_PIPELINE))
                        return PipelineStrategy.EXIT;
                    if (strategy.hasBehaviour(DISCARD_CURRENT))
                        discarded.add(indexed);
                    if (strategy.hasBehaviour(DISCARD_ALL))
                        discarded.addAll(output.index().stream().toList());
                    if (strategy.hasBehaviour(STOP_CURRENT))
                        break;
                    if (strategy.hasBehaviour(STOP_ALL))
                        break STEP_LOOP;
                }
            }

            return PipelineStrategy.CONTINUE;
        }
        finally {
            output.finish();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result runStep(StepDescriptor<Indexable, I, P> step, Indexable indexed, I input, Output<P> output, Context<P> context, PipelineStepMetrics metrics) throws Exception
    {
        String name = getPrintableName(step);

        long start = System.nanoTime();
        try {
            logger.trace(metrics.marker(output.tag()), "{}#{} running step {} over argument {}", this.pipeline.id(), output.tag().uid(), name, indexed.uid());
            Result result = step.execute(indexed, input, output);

            metrics.successCounter().increment();
            return result;
        }
        catch (Exception e) {
            logger.trace(metrics.marker(output.tag(), e), "{}#{} step {} threw an {}: {}", this.pipeline.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            return step.handleException(e, input, output.payload(), output.results(), context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    private static String getPrintableName(StepDescriptor<?, ?, ?> step)
    {
        return step.id() + (step.isPinned() ? " (pinned)" : "");
    }
}
