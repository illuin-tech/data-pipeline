package tech.illuin.pipeline.step.result;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.input.indexer.Indexable;
import tech.illuin.pipeline.input.uid_generator.KSUIDGenerator;
import tech.illuin.pipeline.input.uid_generator.UIDGenerator;

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

        Assertions.assertEquals(2, container.stream(first).count());
        Assertions.assertEquals(3, container.stream(second).count());

        Assertions.assertEquals(5, container.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(5, container.latest(TestResult.class).map(TestResult::value).orElse(-1));

        var secondGenContainer = new ResultContainer();
        secondGenContainer.register(container);

        secondGenContainer.register(first, createDescriptor(new TestResult(6)));
        secondGenContainer.register(second, createDescriptor(new TestResult(7)));

        Assertions.assertEquals(2, secondGenContainer.size());
        Assertions.assertEquals(7, secondGenContainer.stream().count());
        Assertions.assertEquals(2, secondGenContainer.current().count());

        Assertions.assertEquals(7, secondGenContainer.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(7, secondGenContainer.latest(TestResult.class).map(TestResult::value).orElse(-1));
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

        ResultView view = container.view(first).self();

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

        ResultView secondGenView = secondGenContainer.view(first).self();

        Assertions.assertEquals(2, secondGenContainer.size());
        Assertions.assertEquals(7, secondGenContainer.stream().count());
        Assertions.assertEquals(2, secondGenContainer.current().count());
        Assertions.assertEquals(3, secondGenView.stream().count());

        Assertions.assertEquals(6, secondGenView.current(TestResult.class).map(TestResult::value).orElse(-1));
        Assertions.assertEquals(6, secondGenView.latest(TestResult.class).map(TestResult::value).orElse(-1));
    }

    private static <R extends Result> ResultDescriptor<R> createDescriptor(R result)
    {
        return new ResultDescriptor<>(uidGenerator.generate(), result);
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
}
