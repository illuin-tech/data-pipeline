package tech.illuin.pipeline.admin.spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "data-pipeline.admin.enabled=true",
    "data-pipeline.admin.path=/custom-admin",
    "data-pipeline.admin.exclude[0]=excluded-pipeline"
})
public class PipelineAdminSpringTest
{
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private PipelineAdminService adminService;

    @Test
    public void testHtmlEndpoints()
    {
        ResponseEntity<String> response = this.restTemplate.getForEntity("/custom-admin/", String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("data-pipeline Admin"));

        ResponseEntity<String> responseDetails = this.restTemplate.getForEntity("/custom-admin/p2", String.class);
        Assertions.assertEquals(HttpStatus.OK, responseDetails.getStatusCode());
        Assertions.assertTrue(responseDetails.getBody().contains("data-pipeline Admin"));
    }

    @Test
    public void testRestEndpoints()
    {
        ResponseEntity<List> pipelinesResponse = this.restTemplate.getForEntity("/custom-admin/api/pipeline", List.class);
        Assertions.assertEquals(HttpStatus.OK, pipelinesResponse.getStatusCode());
        // We have p1, p2 (from provider), p3. excluded-pipeline is excluded.
        // So p1, p2, p3 = 3 pipelines.
        Assertions.assertEquals(3, pipelinesResponse.getBody().size());

        ResponseEntity<Map> pipelineResponse = this.restTemplate.getForEntity("/custom-admin/api/pipeline/p2", Map.class);
        Assertions.assertEquals(HttpStatus.OK, pipelineResponse.getStatusCode());
        Assertions.assertEquals("p2", pipelineResponse.getBody().get("id"));

        ResponseEntity<Map> kpisResponse = this.restTemplate.getForEntity("/custom-admin/api/kpi", Map.class);
        Assertions.assertEquals(HttpStatus.OK, kpisResponse.getStatusCode());

        ResponseEntity<Map> p2KpisResponse = this.restTemplate.getForEntity("/custom-admin/api/kpi/p2", Map.class);
        Assertions.assertEquals(HttpStatus.OK, p2KpisResponse.getStatusCode());

        ResponseEntity<Map> pingResponse = this.restTemplate.getForEntity("/custom-admin/api/ping", Map.class);
        Assertions.assertEquals(HttpStatus.OK, pingResponse.getStatusCode());
        Assertions.assertEquals("ok", pingResponse.getBody().get("status"));
    }

    @Test
    public void testAssets()
    {
        ResponseEntity<byte[]> assetResponse = this.restTemplate.getForEntity("/custom-admin/assets/logo-255px.png", byte[].class);
        Assertions.assertEquals(HttpStatus.OK, assetResponse.getStatusCode());
        Assertions.assertNotNull(assetResponse.getBody());
        Assertions.assertTrue(assetResponse.getBody().length > 0);
    }

    @SpringBootApplication
    static class TestApp
    {
        @Bean
        public Pipeline<?> p1()
        {
            return Pipeline.of("p1").build();
        }

        @Bean
        public Pipeline<?> p3()
        {
            return Pipeline.of("p3").build();
        }

        @Bean
        public Pipeline<?> excluded()
        {
            return Pipeline.of("excluded-pipeline").build();
        }

        @Bean
        public PipelineProvider customProvider()
        {
            return () -> Collections.singletonList(Pipeline.of("p2").build());
        }
    }
}
