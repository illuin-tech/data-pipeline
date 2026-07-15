package tech.illuin.pipeline.admin.spring;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.PipelineAdminService;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "data-pipeline.admin.enabled=true",
    "data-pipeline.admin.path=/custom-admin",
    "data-pipeline.admin.exclude[0]=excluded-pipeline"
})
public class PipelineAdminSpringTest
{
    @Autowired private PipelineAdminService adminService;
    @Autowired private RestTestClient restTemplate;

    @Test
    public void testHtmlEndpoints()
    {
        this.restTemplate.get().uri("/custom-admin/").exchange()
            .expectStatus().isOk()
            .expectBody(String.class).value(body -> Assertions.assertThat(body).contains("data-pipeline Admin"));

        this.restTemplate.get().uri("/custom-admin/p2").exchange()
            .expectStatus().isOk()
            .expectBody(String.class).value(body -> Assertions.assertThat(body).contains("data-pipeline Admin"));
    }

    @Test
    public void testRestEndpoints()
    {
        this.restTemplate.get().uri("/custom-admin/api/pipeline").exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<Map>>() {})
            // We have p1, p2 (from provider), p3. excluded-pipeline is excluded.
            // So p1, p2, p3 = 3 pipelines.
            .value(list -> Assertions.assertThat(list).hasSize(3));

        this.restTemplate.get().uri("/custom-admin/api/pipeline/p2").exchange()
            .expectStatus().isOk()
            .expectBody(Map.class).value(body -> Assertions.assertThat(body).containsEntry("id", "p2"));

        this.restTemplate.get().uri("/custom-admin/api/kpi").exchange()
            .expectStatus().isOk();

        this.restTemplate.get().uri("/custom-admin/api/kpi/p2").exchange()
            .expectStatus().isOk();

        this.restTemplate.get().uri("/custom-admin/api/ping").exchange()
            .expectStatus().isOk()
            .expectBody(Map.class).value(body -> Assertions.assertThat(body).containsEntry("status", "ok"));
    }

    @Test
    public void testAssets()
    {
        this.restTemplate.get().uri("/custom-admin/assets/logo-255px.png").exchange()
            .expectStatus().isOk()
            .expectBody(byte[].class).value(body -> Assertions.assertThat(body).isNotEmpty());
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
