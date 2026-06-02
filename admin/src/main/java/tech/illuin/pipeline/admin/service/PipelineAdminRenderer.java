package tech.illuin.pipeline.admin.service;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdminRenderer
{
    private final PebbleEngine pebbleEngine;
    private final String rootPath;

    private static final Set<String> ASSETS = Set.of(
        "logo-255px.png"
    );

    public PipelineAdminRenderer(String rootPath)
    {
        this.pebbleEngine = new PebbleEngine.Builder().build();
        this.rootPath = rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath;
    }

    public String renderAdmin() throws IOException
    {
        PebbleTemplate compiledTemplate = this.pebbleEngine.getTemplate("templates/admin.html.peb");
        Writer writer = new StringWriter();
        Map<String, Object> context = new HashMap<>();
        context.put("rootPath", this.rootPath);
        compiledTemplate.evaluate(writer, context);
        return writer.toString();
    }
    
    public String renderPipelineDetails(String pipelineId) throws IOException
    {
        PebbleTemplate compiledTemplate = this.pebbleEngine.getTemplate("templates/details.html.peb");
        Writer writer = new StringWriter();
        Map<String, Object> context = new HashMap<>();
        context.put("pipelineId", pipelineId);
        context.put("rootPath", this.rootPath);
        compiledTemplate.evaluate(writer, context);
        return writer.toString();
    }

    public byte[] readAsset(String assetName) throws IOException
    {
        if (!ASSETS.contains(assetName))
            throw new IllegalArgumentException("Can't access disallowed asset " + assetName);

        String assetPath = "assets/" + assetName;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(assetPath))
        {
            if (is == null)
                throw new IllegalStateException("Could not find asset " + assetPath);
            return is.readAllBytes();
        }
    }
}
