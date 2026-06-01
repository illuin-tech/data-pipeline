package tech.illuin.pipeline.admin.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.controller.AdminAssetController;
import tech.illuin.pipeline.admin.controller.AdminHtmlController;
import tech.illuin.pipeline.admin.controller.AdminRestController;

import static tech.illuin.pipeline.admin.controller.AdminAssetController.ROUTE_PREFIX_ASSET;
import static tech.illuin.pipeline.admin.controller.AdminHtmlController.ROUTE_PREFIX_HTML;
import static tech.illuin.pipeline.admin.controller.AdminRestController.ROUTE_PREFIX_REST;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class AdminRouter
{
    private final AdminRestController restController;
    private final AdminHtmlController htmlController;
    private final AdminAssetController assetController;

    public static final String ROUTE_PREFIX = "/pipeline-admin";

    public AdminRouter(PipelineAdminService service, PipelineAdminRenderer renderer, ObjectMapper objectMapper)
    {
        this.restController = new AdminRestController(service, objectMapper);
        this.htmlController = new AdminHtmlController(renderer);
        this.assetController = new AdminAssetController(renderer);
    }

    public void registerRoutes(HttpServer server)
    {
        server.createContext(ROUTE_PREFIX_REST + "/ping", this.restController::ping);
        server.createContext(ROUTE_PREFIX_REST + "/kpi/", this.restController::getPipelineKpis);
        server.createContext(ROUTE_PREFIX_REST + "/kpi", this.restController::getGlobalKpis);
        server.createContext(ROUTE_PREFIX_REST + "/pipeline/", this.restController::getPipeline);
        server.createContext(ROUTE_PREFIX_REST + "/pipeline", this.restController::listPipelines);
        server.createContext(ROUTE_PREFIX_ASSET + "/", this.assetController::serveAsset);
        server.createContext(ROUTE_PREFIX_HTML + "/", this.htmlController::renderPipelineDetails);
        server.createContext(ROUTE_PREFIX_HTML, this.htmlController::renderAdmin);
    }
}
