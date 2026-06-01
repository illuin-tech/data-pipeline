package tech.illuin.pipeline.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PipelineAdminTest
{
    @Test
    public void testPipelineAdmin() throws Exception
    {
        Pipeline<Object> pipeline = Pipeline.of("test-pipeline").build();

        try (PipelineAdmin admin = new PipelineAdminBuilder()
                .addPipeline(pipeline)
                .setPort(0)
                .build()
                .start()
        ) {
            int port = admin.getPort();
            Assertions.assertTrue(port > 0);

            // Test HTML
            for (String path : List.of("/", "/test-pipeline"))
            {
                URL adminUrl = URI.create("http://localhost:" + port + "/pipeline-admin" + path).toURL();
                HttpURLConnection conn = (HttpURLConnection) adminUrl.openConnection();
                conn.setRequestMethod("GET");
                Assertions.assertEquals(200, conn.getResponseCode(), "Failed on HTML path: " + path);
            }

            // Test API
            for (String path : List.of("/api/pipeline", "/api/pipeline/test-pipeline", "/api/kpi", "/api/kpi/test-pipeline", "/api/ping"))
            {
                URL apiUrl = URI.create("http://localhost:" + port + "/pipeline-admin" + path).toURL();
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                Assertions.assertEquals(200, conn.getResponseCode(), "Failed on API path: " + path);
            }

            // Test Assets
            URL assetUrl = URI.create("http://localhost:" + port + "/pipeline-admin/assets/logo-255px.png").toURL();
            HttpURLConnection conn = (HttpURLConnection) assetUrl.openConnection();
            conn.setRequestMethod("GET");
            Assertions.assertEquals(200, conn.getResponseCode());
        }
    }

    @Test
    public void testFiltering()
    {
        Pipeline<Object> p1 = Pipeline.of("p1").build();
        Pipeline<Object> p2 = Pipeline.of("p2").build();
        Pipeline<Object> p3 = Pipeline.of("p3").build();

        PipelineAdmin admin = new PipelineAdminBuilder()
            .addPipeline(p1)
            .addPipeline(p2)
            .addPipeline(p3)
            .include("p1")
            .include("p2")
            .exclude("p2")
            .build();

        Assertions.assertEquals(1, admin.getPipelines().size());
        Assertions.assertEquals("p1", admin.getPipelines().iterator().next().id());
    }
}
