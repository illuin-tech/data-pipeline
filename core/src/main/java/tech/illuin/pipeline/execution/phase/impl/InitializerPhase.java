package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.context.LocalContext;
import tech.illuin.pipeline.execution.phase.IO;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.initializer.metering.InitializationMarkerManager;
import tech.illuin.pipeline.input.initializer.metering.InitializationMetrics;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.manager.ObservabilityManager;
import tech.illuin.pipeline.metering.tag.MetricTags;
import tech.illuin.pipeline.metering.tag.TagResolver;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.Output;
import tech.illuin.pipeline.output.PipelineTag;
import tech.illuin.pipeline.output.factory.OutputFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class InitializerPhase<I> implements PipelinePhase<I>
{
    private final InitializerDescriptor<I> initializer;
    private final TagResolver<I> tagResolver;
    private final List<Indexer<?>> indexers;
    private final OutputFactory<I> outputFactory;
    private final UIDGenerator uidGenerator;
    private final ObservabilityManager observabilityManager;

    private static final Logger logger = LoggerFactory.getLogger(InitializerPhase.class);

    public InitializerPhase(
        InitializerDescriptor<I> initializer,
        TagResolver<I> tagResolver,
        List<Indexer<?>> indexers,
        OutputFactory<I> outputFactory,
        UIDGenerator uidGenerator,
        ObservabilityManager observabilityManager
    ) {
        this.initializer = initializer;
        this.tagResolver = tagResolver;
        this.indexers = indexers;
        this.outputFactory = outputFactory;
        this.uidGenerator = uidGenerator;
        this.observabilityManager = observabilityManager;
    }

    @Override
    public PipelineStrategy run(IO<I> io, Context context, MetricTags tags) throws Exception
    {
        if (context == null)
            throw new IllegalArgumentException("Runtime context cannot be null");

        ComponentTag tag = this.createTag(io.tag(), this.initializer);
        MetricTags metricTags = new MetricTags();
        this.tagResolver.resolve(metricTags, io.input(), context);
        InitializationMarkerManager markerManager = new InitializationMarkerManager(tag, metricTags);
        InitializationMetrics metrics = new InitializationMetrics(this.observabilityManager.meterRegistry(), markerManager);
        LocalContext localContext = new ComponentContext(context, io.input(), tag, this.uidGenerator, this.observabilityManager, markerManager);

        long start = System.nanoTime();
        metrics.setMDC();
        Span span = this.observabilityManager.tracer().nextSpan().name("initialization_phase");
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            span.tag("uid", tag.pipelineTag().uid());

            Object payload = this.runInitializer(io.input(), tag, localContext, metrics);
            Output output = this.runOutputFactory(io.input(), io.tag(), payload, localContext);
            this.runIndexers(io.tag(), payload, output);

            metrics.successCounter().increment();
            io.setOutput(output);

            return PipelineStrategy.CONTINUE;
        }
        catch (Exception e) {
            metrics.setMDC(e);
            span.event("initialization:error");
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
            span.end();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Object runInitializer(I input, ComponentTag tag, LocalContext context, InitializationMetrics metrics) throws Exception
    {
        Span span = this.observabilityManager.tracer().nextSpan().name(tag.id());
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            span.tag("uid", tag.uid());

            span.event("initializer:run");
            logger.trace("{}#{} initializing payload", tag.pipelineTag().pipeline(), tag.pipelineTag().uid());
            Object payload = this.initializer.execute(input, context, this.uidGenerator);
            span.tag("payload_type", payload == null ? "null" : payload.getClass().getName());

            return payload;
        }
        catch (Exception e) {
            metrics.setMDC(e);
            span.event("initializer:error");
            logger.error("{}#{} initializer {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
            return this.initializer.handleException(e, context, this.uidGenerator);
        }
        finally {
            span.end();
        }
    }

    private Output runOutputFactory(I input, PipelineTag tag, Object payload, LocalContext context)
    {
        Span span = this.observabilityManager.tracer().nextSpan().name("output_factory");
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            span.event("output_factory:run");
            Output output = this.outputFactory.create(tag, input, payload, context);
            span.tag("output_type", output == null ? "null" : output.getClass().getName());

            return output;
        }
        finally {
            span.end();
        }
    }

    private void runIndexers(PipelineTag tag, Object payload, Output output)
    {
        Span span = this.observabilityManager.tracer().nextSpan().name("payload_indexers");
        try (Tracer.SpanInScope scope = this.observabilityManager.tracer().withSpan(span.start()))
        {
            for (Indexer<?> indexer : this.indexers)
            {
                @SuppressWarnings("unchecked")
                Indexer<Object> objectIndexer = (Indexer<Object>) indexer;
                span.event("indexer:run:" + indexer.getClass().getName());
                logger.trace("{}#{} launching indexer {}", tag.pipeline(), tag.uid(), indexer.getClass().getName());
                objectIndexer.index(payload, output.index());
            }
        }
        finally {
            span.end();
        }
    }

    private ComponentTag createTag(PipelineTag pipelineTag, InitializerDescriptor<?> initializer)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, initializer.id(), ComponentFamily.INITIALIZER);
    }
}
