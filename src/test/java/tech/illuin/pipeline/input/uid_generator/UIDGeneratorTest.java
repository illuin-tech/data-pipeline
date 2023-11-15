package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.ksuid.Ksuid;
import com.github.f4b6a3.tsid.Tsid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.builder.VoidPayload;
import tech.illuin.pipeline.generic.pipeline.TestResult;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class UIDGeneratorTest
{
    private static final Logger logger = LoggerFactory.getLogger(UIDGeneratorTest.class);

    @Test
    public void test__ksuid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> KSUIDGenerator.INSTANCE.generate());
        Assertions.assertEquals(27, uid.length());
        Assertions.assertEquals(Ksuid.from(uid).toString(), uid);
    }

    @Test
    public void test__tsid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> TSIDGenerator.INSTANCE.generate());
        Assertions.assertEquals(13, uid.length());
        Assertions.assertEquals(Tsid.from(uid).toString(), uid);
    }

    @Test
    public void test__uuid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDGenerator.INSTANCE.generate());
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void testPipeline__ksuid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object, ?> pipeline = createPipeline("test-ksuid", KSUIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 27);
            Assertions.assertEquals(Ksuid.from(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testPipeline__tsid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object, ?> pipeline = createPipeline("test-tsid", TSIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 13);
            Assertions.assertEquals(Tsid.from(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testPipeline__uuid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object, ?> pipeline = createPipeline("test-uuid", UUIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 36);
            Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    private static Pipeline<Object, VoidPayload> createPipeline(String name, UIDGenerator generator, Consumer<String> uidTest, AtomicInteger counter)
    {
        return Assertions.assertDoesNotThrow(() -> Pipeline.of(name)
            .setUidGenerator(generator)
            .registerStep((input, results, context) -> new TestResult("0", "ok"))
            .registerStep((input, results, context) -> new TestResult("1", "ok"))
            .registerSink((out, ctx) -> out.results().descriptors().current().forEach(rd -> logger.info("Found result {} created at {} and of type {}", rd.uid(), rd.createdAt(), rd.result().getClass().getName())))
            .registerSink((out, ctx) -> out.results().descriptors().current().findFirst().ifPresent(rd -> {
                uidTest.accept(rd.uid());
                counter.incrementAndGet();
            }))
            .build()
        );
    }
}
