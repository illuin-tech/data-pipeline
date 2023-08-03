package tech.illuin.pipeline.step.result;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;
import tech.illuin.pipeline.output.ComponentFamily;
import tech.illuin.pipeline.output.ComponentTag;
import tech.illuin.pipeline.output.PipelineTag;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class ResultTest
{
    private static final UIDGenerator uidGenerator = KSUIDGenerator.INSTANCE;

    @Test
    public void testResultContainer()
    {
        var container = new ResultContainer();

        String first = uidGenerator.generate();
        container.register(first, createDescriptor(new TestResult(0)));
        container.register(first, createDescriptor(new TestResult(1)));

        String second = uidGenerator.generate();
        container.register(second, createDescriptor(new TestResult(3)));
        container.register(second, createDescriptor(new TestResult(4)));
        container.register(second, createDescriptor(new TestResult(5)));

        Assertions.assertEquals(2, container.size());
        Assertions.assertEquals(5, container.stream().count());
        Assertions.assertEquals(5, container.current().count());

        Assertions.assertEquals(2, container.of(first).stream().count());
        Assertions.assertEquals(3, container.of(second).stream().count());

        Assertions.assertEquals(5, container.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(5, container.latest(TestResult.class).map(TestResult::value).orElse(-1));

        var secondGenContainer = new ResultContainer();
        secondGenContainer.register(container);

        secondGenContainer.register(first, createDescriptor(new TestResult(6)));
        secondGenContainer.register(second, createDescriptor(new TestResult(7)));

        Assertions.assertEquals(2, secondGenContainer.size());
        Assertions.assertEquals(7, secondGenContainer.stream().count());
        Assertions.assertEquals(7, secondGenContainer.descriptorStream().count());
        Assertions.assertEquals(2, secondGenContainer.current().count());
        Assertions.assertEquals(2, secondGenContainer.descriptors().current().count());

        Assertions.assertEquals(7, secondGenContainer.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(7, secondGenContainer.descriptors().current(TestResult.class).map(ResultDescriptor::result).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(7, secondGenContainer.latest(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(7, secondGenContainer.descriptors().latest(TestResult.class).map(ResultDescriptor::result).map(TestResult::value).orElse(-1));
    }

    @Test
    public void testResultContainer_byName()
    {
        var container = new ResultContainer();

        String uid = uidGenerator.generate();
        container.register(uid, createDescriptor(new NamedResult(0, "first")));
        container.register(uid, createDescriptor(new NamedResult(1, "second")));

        Assertions.assertEquals(0, container.current("first").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(0, container.current(Names.first).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(0, container.latest("first").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(0, container.latest(Names.first).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, container.stream("first").count());
        Assertions.assertEquals(1, container.stream(Names.first).count());

        Assertions.assertEquals(1, container.current("second").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, container.current(Names.second).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, container.latest("second").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, container.latest(Names.second).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, container.stream("second").count());
        Assertions.assertEquals(1, container.stream(Names.second).count());

        Assertions.assertEquals(-1, container.current("third").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(-1, container.current(Names.third).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(-1, container.latest("third").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(-1, container.latest(Names.third).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(0, container.stream("third").count());
        Assertions.assertEquals(0, container.stream(Names.third).count());

        var secondGenContainer = new ResultContainer();
        secondGenContainer.register(container);

        secondGenContainer.register(uid, createDescriptor(new NamedResult(2, "first")));
        secondGenContainer.register(uid, createDescriptor(new NamedResult(3, "third")));

        Assertions.assertEquals(2, secondGenContainer.current("first").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(2, secondGenContainer.current(Names.first).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(2, secondGenContainer.latest("first").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(2, secondGenContainer.latest(Names.first).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(2, secondGenContainer.stream("first").count());
        Assertions.assertEquals(2, secondGenContainer.stream(Names.first).count());

        Assertions.assertEquals(-1, secondGenContainer.current("second").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(-1, secondGenContainer.current(Names.second).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, secondGenContainer.latest("second").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, secondGenContainer.latest(Names.second).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, secondGenContainer.stream("second").count());
        Assertions.assertEquals(1, secondGenContainer.stream(Names.second).count());

        Assertions.assertEquals(3, secondGenContainer.current("third").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(3, secondGenContainer.current(Names.third).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(3, secondGenContainer.latest("third").map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(3, secondGenContainer.latest(Names.third).map(NamedResult.class::cast).map(NamedResult::id).orElse(-1));
        Assertions.assertEquals(1, secondGenContainer.stream("third").count());
        Assertions.assertEquals(1, secondGenContainer.stream(Names.third).count());
    }

    @Test
    public void testResultView()
    {
        var container = new ResultContainer();

        Entity first = new Entity(uidGenerator.generate());
        container.register(first.uid(), createDescriptor(new TestResult(0)));
        container.register(first.uid(), createDescriptor(new TestResult(1)));

        Entity second = new Entity(uidGenerator.generate());
        container.register(second.uid(), createDescriptor(new TestResult(3)));
        container.register(second.uid(), createDescriptor(new TestResult(4)));
        container.register(second.uid(), createDescriptor(new TestResult(5)));

        Results view = container.of(first);

        Assertions.assertEquals(container.createdAt(), view.currentStart());

        Assertions.assertEquals(2, container.size());
        Assertions.assertEquals(5, container.stream().count());
        Assertions.assertEquals(5, container.current().count());
        Assertions.assertEquals(2, view.stream().count());

        Assertions.assertEquals(2, view.stream(TestResult.class).count());

        Assertions.assertEquals(1, view.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(1, view.latest(TestResult.class).map(TestResult::value).orElse(-1));

        var secondGenContainer = new ResultContainer();
        secondGenContainer.register(container);

        secondGenContainer.register(first.uid(), createDescriptor(new TestResult(6)));
        secondGenContainer.register(second.uid(), createDescriptor(new TestResult(7)));

        Results secondGenView = secondGenContainer.of(first);

        Assertions.assertEquals(2, secondGenContainer.size());
        Assertions.assertEquals(7, secondGenContainer.stream().count());
        Assertions.assertEquals(2, secondGenContainer.current().count());
        Assertions.assertEquals(3, secondGenView.stream().count());

        Assertions.assertEquals(6, secondGenView.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(6, secondGenView.latest(TestResult.class).map(TestResult::value).orElse(-1));
    }

    private static <R extends Result> ResultDescriptor<R> createDescriptor(R result)
    {
        return new ResultDescriptor<>(uidGenerator.generate(), createTag(), result);
    }

    private static ComponentTag createTag()
    {
        return new ComponentTag(null, new PipelineTag(null, null, null), null, ComponentFamily.STEP);
    }

    private record Entity(
        String uid
    ) implements Indexable {}

    private record TestResult(
        int value
    ) implements Result {
        @Override
        public String name()
        {
            return Integer.toString(this.value());
        }
    }

    private record NamedResult(
        int id,
        String value
    ) implements Result {
        @Override
        public String name()
        {
            return this.value();
        }
    }

    private enum Names {
        first, second, third
    }
}
