package tech.illuin.pipeline.admin.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import tech.illuin.pipeline.Pipeline;
import tech.illuin.pipeline.admin.service.provider.PipelineProvider;

import java.util.Collections;

@QuarkusTest
public class PipelineAdminQuarkusTest
{
    @Test
    public void testHtmlEndpoints()
    {
        RestAssured.given()
            .when().get("/pipeline-admin/")
            .then()
            .statusCode(200)
            .body(Matchers.containsString("data-pipeline Admin"));

        RestAssured.given()
            .when().get("/pipeline-admin/p2")
            .then()
            .statusCode(200)
            .body(Matchers.containsString("data-pipeline Admin"));
    }

    @Test
    public void testRestEndpoints()
    {
        RestAssured.given()
            .when().get("/pipeline-admin/api/pipeline")
            .then()
            .statusCode(200)
            .body("size()", Matchers.is(3));

        RestAssured.given()
            .when().get("/pipeline-admin/api/pipeline/p2")
            .then()
            .statusCode(200)
            .body("id", Matchers.is("p2"));

        RestAssured.given()
            .when().get("/pipeline-admin/api/kpi")
            .then()
            .statusCode(200);

        RestAssured.given()
            .when().get("/pipeline-admin/api/kpi/p2")
            .then()
            .statusCode(200);

        RestAssured.given()
            .when().get("/pipeline-admin/api/ping")
            .then()
            .statusCode(200)
            .body("status", Matchers.is("ok"));
    }

    @Test
    public void testAssets()
    {
        RestAssured.given()
            .when().get("/pipeline-admin/assets/logo-255px.png")
            .then()
            .statusCode(200)
            .contentType("image/png")
            .body(Matchers.notNullValue());
    }

    @ApplicationScoped
    public static class TestProducers
    {
        @Produces @Singleton
        public Pipeline p1()
        {
            return Pipeline.of("p1").build();
        }

        @Produces @Singleton
        public Pipeline p3()
        {
            return Pipeline.of("p3").build();
        }

        @Produces @Singleton
        public PipelineProvider customProvider()
        {
            return () -> Collections.singletonList(Pipeline.of("p2").build());
        }
    }
}
