package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.PipelineResult;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineStepMetrics;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
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

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class StepPhase<I> implements PipelinePhase<I>
{
    private final List<StepDescriptor<Indexable, I>> steps;
    private final UIDGenerator uidGenerator;
    private final MeterRegistry meterRegistry;
    
    private static final Logger logger = LoggerFactory.getLogger(StepPhase.class);

    public StepPhase(List<StepDescriptor<Indexable, I>> steps, UIDGenerator uidGenerator, MeterRegistry meterRegistry)
    {
        this.steps = steps;
        this.uidGenerator = uidGenerator;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(I input, Output output, Context context, MetricTags metricTags) throws Exception
    {
        try {
            Set<Indexable> discarded = new HashSet<>();
            STEP_LOOP: for (StepDescriptor<Indexable, I> step : this.steps)
            {
                ComponentTag tag = this.createTag(output.tag(), step);
                PipelineStepMetrics metrics = new PipelineStepMetrics(this.meterRegistry, tag, metricTags);

                /* Arguments are a list of Indexable which satisfy the step's execution predicate */
                List<Indexable> arguments = output.index().stream()
                    .filter(idx -> !discarded.contains(idx) || step.isPinned())
                    .filter(idx -> step.canExecute(idx, context))
                    .toList()
                ;

                logger.trace(metrics.mark(), "{}#{} retrieved {} arguments for step {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), arguments.size(), tag.id());

                /* For each argument we perform the step and register the produced Result */
                for (Indexable indexed : arguments)
                {
                    Result result = this.runStep(step, tag, indexed, input, output, context, metrics);
                    metrics.resultCounter(result).increment();

                    StepStrategy strategy = step.postEvaluation(result, indexed, input, context);
                    logger.trace(metrics.mark(), "{}#{} received {} signal after step {} over argument {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), strategy, tag.id(), indexed.uid());

                    if (strategy.hasBehaviour(REGISTER_RESULT))
                    {
                        if (result instanceof PipelineResult pResult)
                            pResult.output().results().descriptors().current().forEach(rd -> output.results().register(indexed.uid(), rd));
                        else {
                            output.results().register(indexed.uid(), new ResultDescriptor<>(
                                this.uidGenerator.generate(),
                                tag,
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
    private Result runStep(StepDescriptor<Indexable, I> step, ComponentTag tag, Indexable indexed, I input, Output output, Context context, PipelineStepMetrics metrics) throws Exception
    {
        ComponentContext componentContext = wrapContext(input, context, tag.pipelineTag(), tag, metrics);
        String name = getPrintableName(step);

        long start = System.nanoTime();
        try {
            logger.trace(metrics.mark(), "{}#{} running step {} over argument {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), name, indexed.uid());
            Result result = step.execute(indexed, input, output, componentContext);

            metrics.successCounter().increment();
            return result;
        }
        catch (Exception e) {
            logger.trace(metrics.mark(e), "{}#{} step {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            return step.handleException(e, input, output.payload(), output.results(), componentContext);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    private ComponentTag createTag(PipelineTag pipelineTag, StepDescriptor<?, ?> step)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, step.id(), ComponentFamily.STEP);
    }

    private ComponentContext wrapContext(I input, Context context, PipelineTag pipelineTag, ComponentTag componentTag, LogMarker marker)
    {
        return new ComponentContext(context, input, pipelineTag, componentTag, this.uidGenerator, marker);
    }

    private static String getPrintableName(StepDescriptor<?, ?> step)
    {
        return step.id() + (step.isPinned() ? " (pinned)" : "");
    }
}
