package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PhaseException;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.sink.SinkException;
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
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(SinkPhase.class);

    public SinkPhase(Pipeline<I, P> pipeline, List<SinkDescriptor<P>> sinks, ExecutorService sinkExecutor, int closeTimeout, MeterRegistry meterRegistry)
    {
        this.pipeline = pipeline;
        this.sinks = sinks;
        this.sinkExecutor = sinkExecutor;
        this.closeTimeout = closeTimeout;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(I input, Output<P> output, Context<P> context) throws PhaseException
    {
        for (SinkDescriptor<P> descriptor : this.sinks)
        {
            PipelineSinkMetrics metrics = new PipelineSinkMetrics(this.meterRegistry, this.pipeline, descriptor);

            if (descriptor.isAsync())
                this.runSinkAsynchronously(descriptor, output, context, metrics);
            else
                this.runSinkSynchronously(descriptor, output, context, metrics);
        }

        return PipelineStrategy.CONTINUE;
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkSynchronously(SinkDescriptor<P> sink, Output<P> output, Context<P> context, PipelineSinkMetrics metrics) throws SinkException
    {
        String name = getPrintableName(sink);
        long start = System.nanoTime();
        try {
            logger.trace("{}#{} launching sink {}", this.pipeline.id(), output.tag().uid(), name);
            sink.execute(output);
            metrics.successCounter().increment();
        }
        catch (SinkException | RuntimeException e) {
            logger.error("{}#{} sink {} threw an {}: {}", this.pipeline.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            sink.handleException(e, output, context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor<P> sink, Output<P> output, Context<P> context, PipelineSinkMetrics metrics)
    {
        String name = getPrintableName(sink);
        logger.trace("{}#{} queuing sink {}", this.pipeline.id(), output.tag().uid(), name);
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            try {
                logger.trace("{}#{} launching sink {}", this.pipeline.id(), output.tag().uid(), name);
                sink.execute(output);
                metrics.successCounter().increment();
            }
            catch (SinkException | RuntimeException e) {
                logger.error("{}#{} sink {} threw an {}: {}", this.pipeline.id(), output.tag().uid(), name, e.getClass().getName(), e.getMessage());
                metrics.failureCounter().increment();
                sink.handleExceptionThenSwallow(e, output, context);
            }
            finally {
                metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                metrics.totalCounter().increment();
            }
        }, this.sinkExecutor);
    }

    private static String getPrintableName(SinkDescriptor<?> sink)
    {
        return sink.id() + (sink.isAsync() ? " (async)" : "");
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
