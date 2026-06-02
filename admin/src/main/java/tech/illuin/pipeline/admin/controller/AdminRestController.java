package tech.illuin.pipeline.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.admin.controller.response.HttpException;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.illuin.pipeline.admin.router.AdminRouter.ROUTE_PREFIX;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AdminRestController
{
    private final PipelineAdminService service;
    private final ObjectMapper objectMapper;

    public static final String ROUTE_PREFIX_REST = ROUTE_PREFIX + "/api";
    private static final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    public AdminRestController(PipelineAdminService service, ObjectMapper objectMapper)
    {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    public void listPipelines(HttpExchange exchange) throws IOException
    {
        List<PipelineDescription> descriptions = this.service.listPipelines();
        sendJsonResponse(exchange, descriptions);
    }

    public void getPipeline(HttpExchange exchange) throws IOException
    {
        try {
            String path = exchange.getRequestURI().getPath();
            String prefix = ROUTE_PREFIX_REST + "/pipeline/";
            if (path.length() <= prefix.length())
                throw new HttpException(404, "Invalid pipeline ID");

            String id = path.substring(prefix.length());

            PipelineDescription description = this.service.getPipeline(id).orElseThrow(() -> new HttpException(404, "Pipeline not found"));
            this.sendJsonResponse(exchange, description);
        }
        catch (HttpException e) {
            exchange.sendResponseHeaders(e.getCode(), -1);
            logger.error("Controller error: {}", e.getMessage());
        }
    }

    public void getGlobalKpis(HttpExchange exchange) throws IOException
    {
        Map<String, Object> kpis = this.service.getGlobalKpis();
        sendJsonResponse(exchange, kpis);
    }

    public void getPipelineKpis(HttpExchange exchange) throws IOException
    {
        try {
            String path = exchange.getRequestURI().getPath();
            String prefix = ROUTE_PREFIX_REST + "/kpi/";
            if (path.length() <= prefix.length())
                throw new HttpException(404, "Invalid request path");

            String id = path.substring(prefix.length());

            Map<String, Object> kpis = this.service.getPipelineKpis(id).orElseThrow(() -> new HttpException(404, "Pipeline not found"));
            sendJsonResponse(exchange, kpis);
        }
        catch (HttpException e) {
            exchange.sendResponseHeaders(e.getCode(), -1);
            logger.error("Controller error: {}", e.getMessage());
        }
    }

    public void ping(HttpExchange exchange) throws IOException
    {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        sendJsonResponse(exchange, status);
    }

    private void sendJsonResponse(HttpExchange exchange, Object body) throws IOException
    {
        byte[] response = this.objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}