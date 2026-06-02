package tech.illuin.pipeline.admin.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.illuin.pipeline.admin.service.PipelineAdminRenderer;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
@RestController
@RequestMapping("${data-pipeline.admin.path:/pipeline-admin}")
public class PipelineAdminController
{
    private final PipelineAdminService service;
    private final PipelineAdminRenderer renderer;

    @Autowired
    public PipelineAdminController(
        PipelineAdminService service,
        PipelineAdminRenderer renderer
    ) {
        this.service = service;
        this.renderer = renderer;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String renderAdmin() throws IOException
    {
        return this.renderer.renderAdmin();
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String renderPipelineDetails(@PathVariable("id") String id) throws IOException
    {
        return this.renderer.renderPipelineDetails(id);
    }

    @GetMapping("/api/pipeline")
    public List<PipelineDescription> listPipelines()
    {
        return this.service.listPipelines();
    }

    @GetMapping("/api/pipeline/{id}")
    public ResponseEntity<PipelineDescription> getPipeline(@PathVariable("id") String id)
    {
        return this.service.getPipeline(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/kpi")
    public Map<String, Object> getGlobalKpis()
    {
        return this.service.getGlobalKpis();
    }

    @GetMapping("/api/kpi/{id}")
    public ResponseEntity<Map<String, Object>> getPipelineKpis(@PathVariable("id") String id)
    {
        return this.service.getPipelineKpis(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build())
        ;
    }

    @GetMapping("/api/ping")
    public Map<String, String> ping()
    {
        return Map.of("status", "ok");
    }

    @GetMapping("/assets/{filename:.+}")
    public ResponseEntity<byte[]> serveAsset(@PathVariable("filename") String filename) throws IOException
    {
        byte[] asset = this.renderer.readAsset(filename);
        if (asset == null)
            return ResponseEntity.notFound().build();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (filename.endsWith(".png"))
            mediaType = MediaType.IMAGE_PNG;
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            mediaType = MediaType.IMAGE_JPEG;
        else if (filename.endsWith(".svg"))
            mediaType = MediaType.valueOf("image/svg+xml");

        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(asset);
    }
}
