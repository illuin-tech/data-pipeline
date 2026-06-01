package tech.illuin.pipeline.admin.micronaut.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Controller("${data-pipeline.admin.path:/pipeline-admin}")
@ExecuteOn(TaskExecutors.BLOCKING)
public class PipelineAdminController
{
    private final PipelineAdminService service;
    private final PipelineAdminRenderer renderer;

    public PipelineAdminController(
        PipelineAdminService service,
        PipelineAdminRenderer renderer
    ) {
        this.service = service;
        this.renderer = renderer;
    }

    @Get(value = "/", produces = MediaType.TEXT_HTML)
    public String renderAdmin() throws IOException
    {
        return this.renderer.renderAdmin();
    }

    @Get(value = "/{id}", produces = MediaType.TEXT_HTML)
    public String renderPipelineDetails(@PathVariable("id") String id) throws IOException
    {
        return this.renderer.renderPipelineDetails(id);
    }

    @Get(value = "/api/pipeline", produces = MediaType.APPLICATION_JSON)
    public List<PipelineDescription> listPipelines()
    {
        return this.service.listPipelines();
    }

    @Get(value = "/api/pipeline/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<PipelineDescription> getPipeline(@PathVariable("id") String id)
    {
        return this.service.getPipeline(id)
            .map(HttpResponse::ok)
            .orElse(HttpResponse.notFound());
    }

    @Get(value = "/api/kpi", produces = MediaType.APPLICATION_JSON)
    public Map<String, Object> getGlobalKpis()
    {
        return this.service.getGlobalKpis();
    }

    @Get(value = "/api/kpi/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> getPipelineKpis(@PathVariable("id") String id)
    {
        return this.service.getPipelineKpis(id)
            .map(HttpResponse::ok)
            .orElseGet(HttpResponse::notFound)
        ;
    }

    @Get(value = "/api/ping", produces = MediaType.APPLICATION_JSON)
    public Map<String, String> ping()
    {
        return Map.of("status", "ok");
    }

    @Get("/assets/{filename}")
    public HttpResponse<byte[]> serveAsset(@PathVariable("filename") String filename) throws IOException
    {
        byte[] asset = this.renderer.readAsset(filename);

        String mediaType = "application/octet-stream";
        if (filename.endsWith(".png"))
            mediaType = "image/png";
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            mediaType = "image/jpeg";
        else if (filename.endsWith(".svg"))
            mediaType = "image/svg+xml";

        return HttpResponse.ok(asset).contentType(mediaType);
    }
}
