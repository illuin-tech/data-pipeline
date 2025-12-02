package tech.illuin.pipeline.input.uid_generator;

import com.github.f4b6a3.ksuid.Ksuid;
import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.uuid.enums.UuidLocalDomain;
import com.github.f4b6a3.uuid.enums.UuidNamespace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
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
        logger.info("Generated KSUID: {}", uid);
        Assertions.assertEquals(27, uid.length());
        Assertions.assertEquals(Ksuid.from(uid).toString(), uid);
    }

    @Test
    public void test__tsid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> TSIDGenerator.INSTANCE.generate());
        logger.info("Generated TSID: {}", uid);
        Assertions.assertEquals(13, uid.length());
        Assertions.assertEquals(Tsid.from(uid).toString(), uid);
    }

    @Test
    public void test__ulid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> ULIDGenerator.INSTANCE.generate());
        logger.info("Generated ULID: {}", uid);
        Assertions.assertEquals(26, uid.length());
        Assertions.assertEquals(Ulid.from(uid).toString(), uid);
    }

    @Test
    public void test__uuid()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDGenerator.INSTANCE.generate());
        logger.info("Generated UUID: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv1()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDv1Generator.INSTANCE.generate());
        logger.info("Generated UUIDV1: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv2()
    {
        String uid = Assertions.assertDoesNotThrow(() -> new UUIDv2Generator(UuidLocalDomain.LOCAL_DOMAIN_ORG, 0).generate());
        logger.info("Generated UUIDV2: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv3()
    {
        String uid = Assertions.assertDoesNotThrow(() -> new UUIDv3Generator(UuidNamespace.NAMESPACE_DNS, "illuin.tech").generate());
        logger.info("Generated UUIDV3: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv4()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDv4Generator.INSTANCE.generate());
        logger.info("Generated UUIDV4: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv5()
    {
        String uid = Assertions.assertDoesNotThrow(() -> new UUIDv5Generator(UuidNamespace.NAMESPACE_DNS, "illuin.tech").generate());
        logger.info("Generated UUIDV5: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv6()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDv6Generator.INSTANCE.generate());
        logger.info("Generated UUIDV6: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void test__uuidv7()
    {
        String uid = Assertions.assertDoesNotThrow(() -> UUIDv7Generator.INSTANCE.generate());
        logger.info("Generated UUIDV7: {}", uid);
        Assertions.assertEquals(36, uid.length());
        Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
    }

    @Test
    public void testPipeline__ksuid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object> pipeline = createPipeline("test-ksuid", KSUIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 27);
            Assertions.assertEquals(Ksuid.from(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testPipeline__ulid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object> pipeline = createPipeline("test-ulid", ULIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 26);
            Assertions.assertEquals(Ulid.from(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testPipeline__tsid()
    {
        AtomicInteger counter = new AtomicInteger(0);

        Pipeline<Object> pipeline = createPipeline("test-tsid", TSIDGenerator.INSTANCE, uid -> {
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

        Pipeline<Object> pipeline = createPipeline("test-uuid", UUIDGenerator.INSTANCE, uid -> {
            Assertions.assertEquals(uid.length(), 36);
            Assertions.assertEquals(UUID.fromString(uid).toString(), uid);
        }, counter);

        Assertions.assertDoesNotThrow(() -> pipeline.run());
        Assertions.assertDoesNotThrow(pipeline::close);

        Assertions.assertEquals(1, counter.get());
    }

    private static Pipeline<Object> createPipeline(String name, UIDGenerator generator, Consumer<String> uidTest, AtomicInteger counter)
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
