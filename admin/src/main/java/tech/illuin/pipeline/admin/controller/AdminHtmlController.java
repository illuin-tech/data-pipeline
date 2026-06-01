package tech.illuin.pipeline.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.admin.controller.response.HttpException;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static tech.illuin.pipeline.admin.router.AdminRouter.ROUTE_PREFIX;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AdminHtmlController
{
    private final PipelineAdminRenderer renderer;

    public static final String ROUTE_PREFIX_HTML = ROUTE_PREFIX;
    private static final Logger logger = LoggerFactory.getLogger(AdminHtmlController.class);

    public AdminHtmlController(PipelineAdminRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void renderAdmin(HttpExchange exchange) throws IOException
    {
        try {
            String output = this.renderer.renderAdmin();
            
            byte[] response = output.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
        catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
            logger.error("Controller error: {}", e.getMessage(), e);
        }
    }

    public void renderPipelineDetails(HttpExchange exchange) throws IOException
    {
        try {
            String path = exchange.getRequestURI().getPath();
            String contextPath = exchange.getHttpContext().getPath();

            if (path.length() <= contextPath.length())
            {
                this.renderAdmin(exchange);
                return;
            }

            String pipelineId = path.substring(contextPath.length());
            if (pipelineId.isEmpty())
                throw new HttpException(404, "Invalid pipeline ID");
            if (pipelineId.endsWith("/"))
                pipelineId = pipelineId.substring(0, pipelineId.length() - 1);

            String output = this.renderer.renderPipelineDetails(pipelineId);
            
            byte[] response = output.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
        catch (HttpException e) {
            exchange.sendResponseHeaders(e.getCode(), -1);
            logger.error("Controller error: {}", e.getMessage());
        }
        catch (Exception e) {
            exchange.sendResponseHeaders(500, -1);
            logger.error("Controller error: {}", e.getMessage(), e);
        }
    }
}