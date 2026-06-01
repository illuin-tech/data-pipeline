package tech.illuin.pipeline.admin.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;

import java.io.IOException;
import java.io.OutputStream;

import static tech.illuin.pipeline.admin.router.AdminRouter.ROUTE_PREFIX;

public class AdminAssetController
{
    private final PipelineAdminRenderer renderer;

    public static final String ROUTE_PREFIX_ASSET = ROUTE_PREFIX + "/assets";
    private static final Logger logger = LoggerFactory.getLogger(AdminAssetController.class);

    public AdminAssetController(PipelineAdminRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void serveAsset(HttpExchange exchange) throws IOException
    {
        String path = exchange.getRequestURI().getPath().substring(ROUTE_PREFIX_ASSET.length() + 1);
        byte[] bytes = this.renderer.readAsset(path);

        if (path.endsWith(".png"))
            exchange.getResponseHeaders().set("Content-Type", "image/png");
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
            exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
        else if (path.endsWith(".svg"))
            exchange.getResponseHeaders().set("Content-Type", "image/svg+xml");

        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
