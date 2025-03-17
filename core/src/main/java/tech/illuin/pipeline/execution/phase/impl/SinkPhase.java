package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.IO;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineSinkMetrics;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
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
    private final String pipelineId;
    private final List<SinkDescriptor> sinks;
    /* Beware of using a ForkJoinPool, it does not play nice with the Spring ClassLoader ; if absolutely needed a custom ForkJoinWorkerThreadFactory would be required. */
    private final ExecutorService sinkExecutor;
    private final int closeTimeout;
    private final UIDGenerator uidGenerator;
    private final ObservabilityManager observabilityManager;

    private static final Logger logger = LoggerFactory.getLogger(SinkPhase.class);

    public SinkPhase(
        String pipelineId,
        List<SinkDescriptor> sinks,
        Supplier<ExecutorService> sinkExecutorProvider,
        int closeTimeout,
        UIDGenerator uidGenerator,
        ObservabilityManager observabilityManager
    ) {
        this.pipelineId = pipelineId;
        this.sinks = sinks;
        this.sinkExecutor = this.initExecutor(sinkExecutorProvider);
        this.closeTimeout = closeTimeout;
        this.uidGenerator = uidGenerator;
        this.observabilityManager = observabilityManager;
    }

    @Override
    public PipelineStrategy run(IO<I> io, Context context, MetricTags metricTags) throws Exception
    {
        for (SinkDescriptor descriptor : this.sinks)
        {
            ComponentTag tag = this.createTag(io.output().tag(), descriptor);
            PipelineSinkMetrics metrics = new PipelineSinkMetrics(this.observabilityManager.meterRegistry(), tag, metricTags);

            if (descriptor.isAsync())
                this.runSinkAsynchronously(descriptor, tag, io, context, metrics);
            else
                this.runSinkSynchronously(descriptor, tag, io, context, metrics);
        }

        return PipelineStrategy.CONTINUE;
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkSynchronously(SinkDescriptor sink, ComponentTag tag, IO<I> io, Context context, PipelineSinkMetrics metrics) throws Exception
    {
        ComponentContext componentContext = wrapContext(io.input(), context, tag.pipelineTag(), tag);

        long start = System.nanoTime();
        metrics.setMDC();
        Span span = this.observabilityManager.tracer().nextSpan().name(tag.id());
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            span.tag("uid", tag.uid());

            span.event("sink:run_sync");
            logger.trace("{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
            sink.execute(io.output(), componentContext);
            metrics.successCounter().increment();
        }
        catch (Exception e) {
            metrics.setMDC(e);
            span.event("sink:error");
            logger.error("{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            sink.handleException(e, io.output(), context);
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
            span.end();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private void runSinkAsynchronously(SinkDescriptor sink, ComponentTag tag, IO<I> io, Context context, PipelineSinkMetrics metrics)
    {
        if (this.sinkExecutor == null)
            throw new IllegalStateException("An asynchronous run has been initiated but there is no active executor");

        ComponentContext componentContext = wrapContext(io.input(), context, tag.pipelineTag(), tag);

        logger.trace("{}#{} queuing sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            MDC.setContextMap(mdc);
            metrics.setMDC();
            Span span = this.observabilityManager.tracer().nextSpan().name(tag.id());
            try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
            {
                span.tag("uid", tag.uid());

                span.event("sink:run_async");
                logger.trace("{}#{} launching sink {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id());
                sink.execute(io.output(), componentContext);
                metrics.successCounter().increment();
            }
            catch (Exception e) {
                metrics.setMDC(e);
                span.event("sink:error");
                logger.error("{}#{} sink {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
                metrics.failureCounter().increment();
                metrics.errorCounter(e).increment();
                sink.handleExceptionThenSwallow(e, io.output(), componentContext);
            }
            finally {
                metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                metrics.totalCounter().increment();
                metrics.unsetMDC();
                span.end();
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

        logger.info("{} closed (executor termination status: {})", this.pipelineId, status);
    }
}
