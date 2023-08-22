package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.marker.LogMarker;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkPhase<I, P> implements PipelinePhase<I, P>
{
    private final Pipeline<I, P> pipeline;
    private final List<SinkDescriptor<P>> sinks;
    /* Beware of using a ForkJoinPool, it does not play nice with the Spring ClassLoader ; if absolutely needed a custom ForkJoinWorkerThreadFactory would be required. */
    private final ExecutorService sinkExecutor;
    private final int closeTimeout;
    private final UIDGenerator uidGenerator;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(SinkPhase.class);

    public SinkPhase(
        Pipeline<I, P> pipeline,
        List<SinkDescriptor<P>> sinks,
        ExecutorService sinkExecutor,
        int closeTimeout,
        UIDGenerator uidGenerator,
        MeterRegistry meterRegistry
    ) {
        this.pipeline = pipeline;
        this.sinks = sinks;
        this.sinkExecutor = sinkExecutor;
        this.closeTimeout = closeTimeout;
        this.uidGenerator = uidGenerator;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(I input, Output<P> output, Context<P> context, MetricTags metricTags) throws Exception
    {
        for (SinkDescriptor<P> descriptor : this.sinks)
        {
            ComponentTag tag = this.createTag(output.tag(), descriptor);
            PipelineSinkMetrics metrics = new PipelineSinkMetrics(this.meterRegistry, tag, metricTags);

            if (descriptor.isAsync())
                this.runSinkAsynchronously(descriptor, tag, input, output, context, metrics);
            else
                this.runSinkSynchronously(descriptor, tag, input, output, context, metrics);
        }

        return PipelineStrategy.CONTINUE;
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkSynchronously(SinkDescriptor<P> sink, ComponentTag tag, I input, Output<P> output, Context<P> context, PipelineSinkMetrics metrics) throws Exception
    {
        ComponentContext<P> componentContext = wrapContext(input, context, tag.pipelineTag(), tag, metrics);

        long start = System.nanoTime();
        try {
            logger.trace(metrics.mark(), "{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
            sink.execute(output, componentContext);
            metrics.successCounter().increment();
        }
        catch (Exception e) {
            logger.error(metrics.mark(e), "{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            sink.handleException(e, output, context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor<P> sink, ComponentTag tag, I input, Output<P> output, Context<P> context, PipelineSinkMetrics metrics)
    {
        ComponentContext<P> componentContext = wrapContext(input, context, tag.pipelineTag(), tag, metrics);

        logger.trace(metrics.mark(), "{}#{} queuing sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            try {
                logger.trace(metrics.mark(), "{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
                sink.execute(output, componentContext);
                metrics.successCounter().increment();
            }
            catch (Exception e) {
                logger.error(metrics.mark(e), "{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
                metrics.failureCounter().increment();
                metrics.errorCounter(e).increment();
                sink.handleExceptionThenSwallow(e, output, componentContext);
            }
            finally {
                metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                metrics.totalCounter().increment();
            }
        }, this.sinkExecutor);
    }

    private ComponentTag createTag(PipelineTag pipelineTag, SinkDescriptor<?> sink)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, sink.id(), ComponentFamily.SINK);
    }

    private ComponentContext<P> wrapContext(I input, Context<P> context, PipelineTag pipelineTag, ComponentTag componentTag, LogMarker marker)
    {
        return new ComponentContext<>(context, input, pipelineTag, componentTag, this.uidGenerator, marker);
    }

    @Override
    public void close() throws Exception
    {
        if (this.sinkExecutor.isShutdown())
            return;

        this.sinkExecutor.shutdown();
        boolean success = this.sinkExecutor.awaitTermination(this.closeTimeout, TimeUnit.SECONDS);
        var status = success ? "done" : "timeout after " + this.closeTimeout + " seconds";

        logger.info("{} closed (executor termination status: {})", this.pipeline.id(), status);
    }
}
