package tech.illuin.pipeline.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.admin.router.AdminRouter;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
class InternalHttpServer
{
    private final String host;
    private final int port;
    private final ObjectMapper objectMapper;
    private final AdminRouter router;
    private HttpServer server;
    private ExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger(InternalHttpServer.class);

    InternalHttpServer(PipelineAdminService service, PipelineAdminRenderer renderer, String host, int port)
    {
        this.host = host;
        this.port = port;
        this.objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.router = new AdminRouter(service, renderer, this.objectMapper);
    }

    public void start()
    {
        try {
            logger.debug("Starting HTTP server on {}:{}", this.host, this.port);
            this.server = HttpServer.create(new InetSocketAddress(this.host, this.port), 0);
            this.router.registerRoutes(this.server);
            this.executor = Executors.newCachedThreadPool(r -> {
                logger.trace("Spawning new executor thread");
                Thread t = new Thread(r, "data-pipeline-admin");
                t.setDaemon(false);
                return t;
            });
            this.server.setExecutor(this.executor);
            this.server.start();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server", e);
        }
    }

    public InetSocketAddress getAddress()
    {
        return this.server != null ? this.server.getAddress() : null;
    }

    public void stop()
    {
        logger.debug("Stopping HTTP server on {}:{}", this.host, this.port);
        if (this.server != null)
            this.server.stop(0);
        if (this.executor != null)
            this.executor.shutdown();
    }
}
