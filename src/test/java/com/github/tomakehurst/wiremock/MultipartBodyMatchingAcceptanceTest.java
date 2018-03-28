package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MultipartBodyMatchingAcceptanceTest extends AcceptanceTestBase {

    HttpClient httpClient = HttpClientFactory.createClient();

    @Test
    public void handlesAbsenceOfPartsInAMultipartRequest() throws Exception {
        stubFor(post("/empty-multipart")
            .withMultipartRequestBody(aMultipart()
                .withName("bits")
                .withBody(matching(".*")))
            .willReturn(ok())
        );

        HttpUriRequest request = RequestBuilder
            .post("http://localhost:" + wireMockServer.port() + "/empty-multipart")
            .setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
            .setEntity(new StringEntity("", MULTIPART_FORM_DATA))
            .build();

        HttpResponse response = httpClient.execute(request);

        assertThat(response.getStatusLine().getStatusCode(), is(404));
    }
}
