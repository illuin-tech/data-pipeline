package tech.illuin.pipeline.admin.micronaut;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;
import tech.illuin.pipeline.observer.descriptor.model.PipelineDescription;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@MicronautTest
public class PipelineAdminMicronautTest
{
    @Inject @Client("/")
    HttpClient client;

    @Test
    public void testHtmlEndpoints()
    {
        String responseHome = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/"));
        Assertions.assertTrue(responseHome.contains("data-pipeline Admin"));

        String responseDetails = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/p2"));
        Assertions.assertTrue(responseDetails.contains("data-pipeline Admin"));
    }

    @Test
    public void testRestEndpoints()
    {
        List<PipelineDescription> pipelines = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/api/pipeline"), Argument.of(List.class, PipelineDescription.class));
        Assertions.assertEquals(3, pipelines.size());

        PipelineDescription pipeline = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/api/pipeline/p2"), PipelineDescription.class);
        Assertions.assertEquals("p2", pipeline.id());

        Map<String, Object> kpis = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/api/kpi"), Argument.of(Map.class, String.class, Object.class));
        Assertions.assertNotNull(kpis);

        Map<String, Object> p2Kpis = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/api/kpi/p2"), Argument.of(Map.class, String.class, Object.class));
        Assertions.assertNotNull(p2Kpis);

        Map<String, String> ping = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/api/ping"), Argument.of(Map.class, String.class, String.class));
        Assertions.assertEquals("ok", ping.get("status"));
    }

    @Test
    public void testAssets()
    {
        byte[] asset = this.client.toBlocking().retrieve(HttpRequest.GET("/pipeline-admin/assets/logo-255px.png"), byte[].class);
        Assertions.assertNotNull(asset);
        Assertions.assertTrue(asset.length > 0);
    }

    @Factory
    public static class TestPipelines
    {
        @Singleton
        public Pipeline<?> p1()
        {
            return Pipeline.of("p1").build();
        }

        @Singleton
        public Pipeline<?> p3()
        {
            return Pipeline.of("p3").build();
        }

        @Singleton
        public PipelineProvider customProvider()
        {
            return () -> Collections.singletonList(Pipeline.of("p2").build());
        }
    }
}
