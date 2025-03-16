package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.PipelineResult;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.IO;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineStepMetrics;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.step.builder.StepDescriptor;
import tech.illuin.pipeline.step.execution.evaluator.StepStrategy;
import tech.illuin.pipeline.step.result.MultiResult;
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
    private final ObservabilityManager observabilityManager;
    
    private static final Logger logger = LoggerFactory.getLogger(StepPhase.class);

    public StepPhase(List<StepDescriptor<Indexable, I>> steps, UIDGenerator uidGenerator, ObservabilityManager observabilityManager)
    {
        this.steps = steps;
        this.uidGenerator = uidGenerator;
        this.observabilityManager = observabilityManager;
    }

    @Override
    public PipelineStrategy run(IO<I> io, Context context, MetricTags metricTags) throws Exception
    {
        try {
            Set<Indexable> discarded = new HashSet<>();
            STEP_LOOP: for (StepDescriptor<Indexable, I> step : this.steps)
            {
                ComponentTag tag = this.createTag(io.output().tag(), step);
                PipelineStepMetrics metrics = new PipelineStepMetrics(this.observabilityManager.meterRegistry(), tag, metricTags);

                /* Arguments are a list of Indexable which satisfy the step's execution predicate */
                List<Indexable> arguments = io.output().index().stream()
                    .filter(idx -> !discarded.contains(idx) || step.isPinned())
                    .filter(idx -> step.canExecute(idx, context))
                    .toList()
                ;

                logger.trace("{}#{} retrieved {} arguments for step {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), arguments.size(), tag.id());

                /* For each argument we perform the step and register the produced Result */
                for (Indexable indexed : arguments)
                {
                    Result result = this.runStep(step, tag, indexed, io, context, metrics);

                    if (result instanceof MultiResult multi)
                        multi.results().forEach(r -> metrics.resultCounter(r).increment());
                    else
                        metrics.resultCounter(result).increment();

                    StepStrategy strategy = step.postEvaluation(result, indexed, io.input(), context);
                    logger.trace("{}#{} received {} signal after step {} over argument {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), strategy, tag.id(), indexed.uid());

                    if (strategy.hasBehaviour(REGISTER_RESULT))
                    {
                        if (result instanceof PipelineResult pResult)
                            pResult.output().results().descriptors().current().forEach(rd -> io.output().results().register(indexed.uid(), rd));
                        else if (result instanceof MultiResult mResult)
                            mResult.results().forEach(r -> {
                                io.output().results().register(
                                    indexed.uid(),
                                    new ResultDescriptor<>(this.uidGenerator.generate(), tag, Instant.now(), r)
                                );
                            });
                        else {
                            io.output().results().register(
                                indexed.uid(),
                                new ResultDescriptor<>(this.uidGenerator.generate(), tag, Instant.now(), result)
                            );
                        }
                    }
                    if (strategy.hasBehaviour(EXIT_PIPELINE))
                        return PipelineStrategy.EXIT;
                    if (strategy.hasBehaviour(DISCARD_CURRENT))
                        discarded.add(indexed);
                    if (strategy.hasBehaviour(DISCARD_ALL))
                        discarded.addAll(io.output().index().stream().toList());
                    if (strategy.hasBehaviour(STOP_CURRENT))
                        break;
                    if (strategy.hasBehaviour(STOP_ALL))
                        break STEP_LOOP;
                }
            }

            return PipelineStrategy.CONTINUE;
        }
        finally {
            io.output().finish();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Result runStep(StepDescriptor<Indexable, I> step, ComponentTag tag, Indexable indexed, IO<I> io, Context context, PipelineStepMetrics metrics) throws Exception
    {
        ComponentContext componentContext = wrapContext(io.input(), context, tag.pipelineTag(), tag);
        String name = getPrintableName(step);

        long start = System.nanoTime();
        metrics.setMDC();
        Span span = this.observabilityManager.tracer().nextSpan().name(tag.id());
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            span.tag("uid", tag.uid());

            span.event("run_step");
            logger.trace("{}#{} running step {} over argument {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), name, indexed.uid());
            Result result = step.execute(indexed, io.input(), io.output(), componentContext);

            span.tag("result_type", result.getClass().getName());
            metrics.successCounter().increment();
            return result;
        }
        catch (Exception e) {
            metrics.setMDC(e);
            logger.error("{}#{} step {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            return step.handleException(e, io.input(), io.output().payload(), io.output().results(), componentContext);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
            span.end();
        }
    }

    private ComponentTag createTag(PipelineTag pipelineTag, StepDescriptor<?, ?> step)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, step.id(), ComponentFamily.STEP);
    }

    private ComponentContext wrapContext(I input, Context context, PipelineTag pipelineTag, ComponentTag componentTag)
    {
        return new ComponentContext(context, input, pipelineTag, componentTag, this.uidGenerator);
    }

    private static String getPrintableName(StepDescriptor<?, ?> step)
    {
        return step.id() + (step.isPinned() ? " (pinned)" : "");
    }
}
