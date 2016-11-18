package com.github.tomakehurst.wiremock;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SpringRestTemplateTest extends AcceptanceTestBase {

    ExecutorService executorService = Executors.newFixedThreadPool(200);

    @Test
    public void restTemplate() throws Exception {
        wm.stubFor(put(urlEqualTo("/put-this"))
            .withRequestBody(equalTo("things"))
            .willReturn(aResponse().withStatus(200)));

        final RestTemplate restTemplate = new RestTemplate();

        List<Future<Integer>> futures = newArrayList();
        for (int i = 0; i < 2000; i++) {
            final int count = i;
            futures.add(executorService.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    RequestEntity<String> request =
                        new RequestEntity<>(
                            "things",
                            HttpMethod.PUT,
                            URI.create("http://localhost:" + wireMockServer.port() + "/put-this")
                        );
                    ResponseEntity<String> response = restTemplate.exchange(request, String.class);
                    System.out.println("Sent " + count);

                    return response.getStatusCode().value();
                }
            }));
        }

        for (Future<Integer> future: futures) {
            assertThat(future.get(), is(200));
        }
    }
}
