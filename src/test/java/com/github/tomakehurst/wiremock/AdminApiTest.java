package com.github.tomakehurst.wiremock;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.toomuchcoding.jsonassert.JsonAssertion;
import com.toomuchcoding.jsonassert.JsonVerifiable;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class AdminApiTest extends AcceptanceTestBase {

    static Stubbing dsl = wireMockServer;

    @Test
    public void getAllStubMappings() throws Exception {
        dsl.stubFor(get(urlEqualTo("/my-test-url")).willReturn(aResponse().withStatus(418)));

        String body = testClient.get("/__admin/mappings").content();

        JSONAssert.assertEquals(
            "{                                      \n" +
            "  \"mappings\" : [ {                   \n" +
            "    \"request\" : {                    \n" +
            "      \"url\" : \"/my-test-url\",      \n" +
            "      \"method\" : \"GET\"             \n" +
            "    },                                 \n" +
            "    \"response\" : {                   \n" +
            "      \"status\" : 418                 \n" +
            "    }                                  \n" +
            "  } ],                                 \n" +
            "                                       \n" +
            "  \"meta\": {                          \n" +
            "    \"total\": 1                       \n" +
            "  }                                    \n" +
            "}",
        body,
            true
        );
    }

    @Test
    public void getAllStubMappingsWithLimitedResults() throws Exception {
        for (int i = 1; i <= 20; i++) {
            dsl.stubFor(get(urlEqualTo("/things/" + i)).willReturn(aResponse().withStatus(418)));
        }

        String allBody = testClient.get("/__admin/mappings").content();
        String limitedBody = testClient.get("/__admin/mappings?limit=7").content();

        JsonAssertion.assertThat(allBody).field("mappings").array().hasSize(20);
        JsonAssertion.assertThat(limitedBody).field("mappings").array().hasSize(7);
    }

    @Test
    public void getAllStubMappingsWithLimitedAndOffsetResults() throws Exception {
        for (int i = 1; i <= 20; i++) {
            dsl.stubFor(get(urlEqualTo("/things/" + i)).willReturn(aResponse().withStatus(418)));
        }

        String limitedBody = testClient.get("/__admin/mappings?limit=4&offset=3").content();

        JsonAssertion.assertThat(limitedBody).field("mappings").array().hasSize(4);
        JsonAssertion.assertThat(limitedBody).field("mappings").elementWithIndex(0)
            .field("request").field("url").isEqualTo("/things/17");
        JsonAssertion.assertThat(limitedBody).field("mappings").elementWithIndex(3)
            .field("request").field("url").isEqualTo("/things/14");
    }

    @Test
    public void deprecatedGetAllStubMappings() throws Exception {
        dsl.stubFor(get(urlEqualTo("/my-test-url")).willReturn(aResponse().withStatus(418)));

        String body = testClient.get("/__admin/").content();
        System.out.println(body);
        JSONAssert.assertEquals(
            "{\n" +
            "  \"mappings\" : [ {\n" +
            "    \"request\" : {\n" +
            "      \"url\" : \"/my-test-url\",\n" +
            "      \"method\" : \"GET\"\n" +
            "    },\n" +
            "    \"response\" : {\n" +
            "      \"status\" : 418\n" +
            "    }\n" +
            "  } ]\n" +
            "}",
            body,
            false
        );
    }

    @Test
    public void getStubMappingById() throws Exception {
        UUID id = UUID.randomUUID();

        dsl.stubFor(trace(urlEqualTo("/my-addressable-stub"))
            .withId(id)
            .willReturn(aResponse().withStatus(451))
        );

        String body = testClient.get("/__admin/mappings/" + id).content();

        JSONAssert.assertEquals(
            "{                                          \n" +
            "    \"uuid\": \"" + id + "\",              \n" +
            "    \"request\" : {                        \n" +
            "      \"url\" : \"/my-addressable-stub\",  \n" +
            "      \"method\" : \"TRACE\"               \n" +
            "    },                                     \n" +
            "    \"response\" : {                       \n" +
            "      \"status\" : 451                     \n" +
            "    }                                      \n" +
            "}",
            body,
            true
        );
    }

    @Test
    public void getLoggedRequests() throws Exception {
        for (int i = 1; i <= 5; i++) {
            testClient.get("/received-request/" + i);
        }

        String body = testClient.get("/__admin/requests").content();

        System.out.println(body);
        JsonVerifiable check = JsonAssertion.assertThat(body);
        check.field("meta").field("total").isEqualTo(5);
        check.field("servedStubs").elementWithIndex(2).field("request").field("url").isEqualTo("/received-request/3");
        check.field("servedStubs").hasSize(5);
    }

    @Test
    public void getLoggedRequestsWithLimit() throws Exception {
        for (int i = 1; i <= 7; i++) {
            testClient.get("/received-request/" + i);
        }

        String body = testClient.get("/__admin/requests?limit=2").content();

        JsonVerifiable check = JsonAssertion.assertThat(body);
        check.field("meta").field("total").isEqualTo(7);
        check.field("servedStubs").elementWithIndex(0).field("request").field("url").isEqualTo("/received-request/7");
        check.field("servedStubs").elementWithIndex(1).field("request").field("url").isEqualTo("/received-request/6");
        check.field("servedStubs").hasSize(2);
    }

    @Test
    public void getLoggedRequestsWithLimitAndSinceDate() throws Exception {
        for (int i = 1; i <= 5; i++) {
            testClient.get("/received-request/" + i);
        }

        String midPoint = new ISO8601DateFormat().format(new Date());

        for (int i = 6; i <= 9; i++) {
            testClient.get("/received-request/" + i);
        }

        String body = testClient.get("/__admin/requests?since=" + midPoint + "&limit=3").content();

        JsonVerifiable check = JsonAssertion.assertThat(body);
        check.field("meta").field("total").isEqualTo(9);
        check.field("servedStubs").hasSize(3);
        check.field("servedStubs").elementWithIndex(0).field("request").field("url").isEqualTo("/received-request/9");
        check.field("servedStubs").elementWithIndex(2).field("request").field("url").isEqualTo("/received-request/7");
    }

    @Test
    public void getLoggedRequestsWithLimitLargerThanResults() throws Exception {
        for (int i = 1; i <= 3; i++) {
            testClient.get("/received-request/" + i);
        }

        String body = testClient.get("/__admin/requests?limit=3000").content();

        JsonVerifiable check = JsonAssertion.assertThat(body);
        check.field("meta").field("total").isEqualTo(3);
        check.field("servedStubs").hasSize(3);
    }

}
