package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.toomuchcoding.jsonassert.JsonAssertion;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertThat;

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

}
