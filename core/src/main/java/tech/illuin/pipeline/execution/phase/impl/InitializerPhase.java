package tech.illuin.pipeline.execution.phase.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.context.ComponentContext;
import tech.illuin.pipeline.context.Context;
import tech.illuin.pipeline.execution.phase.IO;
import tech.illuin.pipeline.execution.phase.PipelinePhase;
import tech.illuin.pipeline.execution.phase.PipelineStrategy;
import tech.illuin.pipeline.input.indexer.Indexer;
import tech.illuin.pipeline.input.initializer.builder.InitializerDescriptor;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.metering.PipelineInitializationMetrics;
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
    private final MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(InitializerPhase.class);

    public InitializerPhase(
        InitializerDescriptor<I> initializer,
        TagResolver<I> tagResolver,
        List<Indexer<?>> indexers,
        OutputFactory<I> outputFactory,
        UIDGenerator uidGenerator,
        MeterRegistry meterRegistry
    ) {
        this.initializer = initializer;
        this.tagResolver = tagResolver;
        this.indexers = indexers;
        this.outputFactory = outputFactory;
        this.uidGenerator = uidGenerator;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PipelineStrategy run(IO<I> io, Context context, MetricTags tags) throws Exception
    {
        if (context == null)
            throw new IllegalArgumentException("Runtime context cannot be null");

        ComponentTag componentTag = this.createTag(io.tag(), this.initializer);
        MetricTags metricTags = this.tagResolver.resolve(io.input(), context);
        PipelineInitializationMetrics metrics = new PipelineInitializationMetrics(this.meterRegistry, componentTag, metricTags);

        long start = System.nanoTime();
        metrics.setMDC();
        try {
            Object payload = this.runInitializer(io.input(), componentTag, context, metrics);
            Output output = this.outputFactory.create(io.tag(), io.input(), payload, context);

            for (Indexer<?> indexer : this.indexers)
            {
                @SuppressWarnings("unchecked")
                Indexer<Object> objectIndexer = (Indexer<Object>) indexer;
                logger.trace("{}#{} launching indexer {}", io.tag().pipeline(), io.tag().uid(), indexer.getClass().getName());
                objectIndexer.index(payload, output.index());
            }

            metrics.successCounter().increment();
            io.setOutput(output);

            return PipelineStrategy.CONTINUE;
        }
        catch (Exception e) {
            metrics.setMDC(e);
            metrics.failureCounter().increment();
            metrics.errorCounter(e).increment();
            throw e;
        }
        finally {
            metrics.runTimer().record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            metrics.totalCounter().increment();
            metrics.unsetMDC();
        }
    }

    @SuppressWarnings("IllegalCatch")
    private Object runInitializer(I input, ComponentTag tag, Context context, PipelineInitializationMetrics metrics) throws Exception
    {
        ComponentContext componentContext = wrapContext(input, context, tag.pipelineTag(), tag);
        try {
            logger.trace("{}#{} initializing payload", tag.pipelineTag().pipeline(), tag.pipelineTag().uid());
            return this.initializer.execute(input, componentContext, this.uidGenerator);
        }
        catch (Exception e) {
            metrics.setMDC(e);
            logger.error("{}#{} initializer {} threw an {}: {}", tag.pipelineTag().pipeline(), tag.pipelineTag().uid(), tag.id(), e.getClass().getName(), e.getMessage());
            return this.initializer.handleException(e, componentContext, this.uidGenerator);
        }
    }

    private ComponentTag createTag(PipelineTag pipelineTag, InitializerDescriptor<?> initializer)
    {
        return new ComponentTag(this.uidGenerator.generate(), pipelineTag, initializer.id(), ComponentFamily.INITIALIZER);
    }

    private ComponentContext wrapContext(I input, Context context, PipelineTag pipelineTag, ComponentTag componentTag)
    {
        return new ComponentContext(context, input, pipelineTag, componentTag, this.uidGenerator);
    }
}
