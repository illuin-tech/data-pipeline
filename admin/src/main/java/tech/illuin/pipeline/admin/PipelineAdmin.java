package tech.illuin.pipeline.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static tech.illuin.pipeline.admin.router.AdminRouter.ROUTE_PREFIX;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdmin implements AutoCloseable
{
    private final String host;
    private final int port;
    private final PipelineProvider provider;
    private final InternalHttpServer server;
    private final CountDownLatch stopLatch;

    private static final Logger logger = LoggerFactory.getLogger(PipelineAdmin.class);

    PipelineAdmin(PipelineProvider provider, String host, int port)
    {
        this.host = host;
        this.port = port;
        this.provider = provider;
        this.server = new InternalHttpServer(new PipelineAdminService(this.provider), new PipelineAdminRenderer(ROUTE_PREFIX), host, port);
        this.stopLatch = new CountDownLatch(1);
    }

    public PipelineAdmin start()
    {
        logger.info("Starting PipelineAdmin on {}:{}", this.host, this.port);
        this.server.start();
        logger.info("PipelineAdmin started on port {}", getPort());
        return this;
    }

    public int getPort()
    {
        return this.server.getAddress() != null ? this.server.getAddress().getPort() : this.port;
    }

    public void stop()
    {
        logger.info("Stopping PipelineAdmin");
        this.server.stop();
        this.stopLatch.countDown();
    }

    public void await() throws InterruptedException
    {
        this.stopLatch.await();
    }

    @Override
    public void close()
    {
        stop();
    }

    public Collection<? extends Pipeline<?>> getPipelines()
    {
        return this.provider.getPipelines();
    }
}
