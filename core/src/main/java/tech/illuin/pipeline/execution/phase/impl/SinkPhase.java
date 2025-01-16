package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.sink.builder.SinkDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class SinkPhase<I> implements PipelinePhase<I>
{
    private final Pipeline<I> pipeline;
    private final List<SinkDescriptor> sinks;
    /* Beware of using a ForkJoinPool, it does not play nice with the Spring ClassLoader ; if absolutely needed a custom ForkJoinWorkerThreadFactory would be required. */
    private final ExecutorService sinkExecutor;
    private final int closeTimeout;
    private final UIDGenerator uidGenerator;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(SinkPhase.class);

    public SinkPhase(
        Pipeline<I> pipeline,
        List<SinkDescriptor> sinks,
        Supplier<ExecutorService> sinkExecutorProvider,
        int closeTimeout,
        UIDGenerator uidGenerator,
        MeterRegistry meterRegistry
    ) {
        this.pipeline = pipeline;
        this.sinks = sinks;
        this.sinkExecutor = this.initExecutor(sinkExecutorProvider);
        this.closeTimeout = closeTimeout;
        this.uidGenerator = uidGenerator;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(I input, Output output, Context context, MetricTags metricTags) throws Exception
    {
        for (SinkDescriptor descriptor : this.sinks)
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
    private void runSinkSynchronously(SinkDescriptor sink, ComponentTag tag, I input, Output output, Context context, PipelineSinkMetrics metrics) throws Exception
    {
        ComponentContext componentContext = wrapContext(input, context, tag.pipelineTag(), tag);

        long start = System.nanoTime();
        metrics.setMDC();
        try {
            logger.trace("{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
            sink.execute(output, componentContext);
            metrics.successCounter().increment();
        }
        catch (Exception e) {
            metrics.setMDC(e);
            logger.error("{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            sink.handleException(e, output, context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor sink, ComponentTag tag, I input, Output output, Context context, PipelineSinkMetrics metrics)
    {
        if (this.sinkExecutor == null)
            throw new IllegalStateException("An asynchronous run has been initiated but there is no active executor");

        ComponentContext componentContext = wrapContext(input, context, tag.pipelineTag(), tag);

        logger.trace("{}#{} queuing sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            MDC.setContextMap(mdc);
            metrics.setMDC();
            try {
                logger.trace("{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
                sink.execute(output, componentContext);
                metrics.successCounter().increment();
            }
            catch (Exception e) {
                metrics.setMDC(e);
                logger.error("{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
                metrics.failureCounter().increment();
                metrics.errorCounter(e).increment();
                sink.handleExceptionThenSwallow(e, output, componentContext);
            }
            finally {
                metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                metrics.totalCounter().increment();
                metrics.unsetMDC();
            }
        }, this.sinkExecutor);
    }

    private ComponentTag createTag(PipelineTag pipelineTag, SinkDescriptor sink)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, sink.id(), ComponentFamily.SINK);
    }

    private ComponentContext wrapContext(I input, Context context, PipelineTag pipelineTag, ComponentTag componentTag)
    {
        return new ComponentContext(context, input, pipelineTag, componentTag, this.uidGenerator);
    }

    private ExecutorService initExecutor(Supplier<ExecutorService> provider)
    {
        if (this.sinks.stream().anyMatch(SinkDescriptor::isAsync))
            return provider.get();
        return null;
    }

    @Override
    public void close() throws Exception
    {
        if (this.sinkExecutor == null)
            return;
        if (this.sinkExecutor.isShutdown())
            return;

        this.sinkExecutor.shutdown();
        boolean success = this.sinkExecutor.awaitTermination(this.closeTimeout, TimeUnit.SECONDS);
        var status = success ? "done" : "timeout after " + this.closeTimeout + " seconds";

        logger.info("{} closed (executor termination status: {})", this.pipeline.id(), status);
    }
}
