package tech.illuin.pipeline.admin.quarkus.rest;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Note: RESTEasy Classic does not support property interpolation in @Path.
 * As a result, the admin path is hardcoded to /pipeline-admin in the Quarkus implementation.
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@Path("/pipeline-admin")
@Singleton
public class PipelineAdminResource
{
    @Inject PipelineAdminService service;
    @Inject PipelineAdminRenderer renderer;

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public String renderAdmin() throws IOException
    {
        return this.renderer.renderAdmin();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String renderPipelineDetails(@PathParam("id") String id) throws IOException
    {
        return this.renderer.renderPipelineDetails(id);
    }

    @GET
    @Path("/api/pipeline")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PipelineDescription> listPipelines()
    {
        return this.service.listPipelines();
    }

    @GET
    @Path("/api/pipeline/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPipeline(@PathParam("id") String id)
    {
        return this.service.getPipeline(id)
            .map(p -> Response.ok(p).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/api/kpi")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getGlobalKpis()
    {
        return this.service.getGlobalKpis();
    }

    @GET
    @Path("/api/kpi/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPipelineKpis(@PathParam("id") String id)
    {
        return this.service.getPipelineKpis(id)
            .map(kpis -> Response.ok(kpis).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build())
        ;
    }

    @GET
    @Path("/api/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> ping()
    {
        return Map.of("status", "ok");
    }

    @GET
    @Path("/assets/{filename}")
    public Response serveAsset(@PathParam("filename") String filename) throws IOException
    {
        byte[] asset = this.renderer.readAsset(filename);

        String mediaType = "application/octet-stream";
        if (filename.endsWith(".png"))
            mediaType = "image/png";
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            mediaType = "image/jpeg";
        else if (filename.endsWith(".svg"))
            mediaType = "image/svg+xml";

        return Response.ok(asset).type(mediaType).build();
    }
}
