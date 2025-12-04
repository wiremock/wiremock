package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.client.okhttp.OkHttpClientFactory;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class LooseUrlAcceptanceTest extends AcceptanceTestBase {

  private WireMockServer proxyingService;
  private OkHttpClient client;
  private String proxyTargetUrl;

  private void proxyServerStart(WireMockConfiguration config) {
    proxyingService = new WireMockServer(config.dynamicPort().httpClientFactory(new OkHttpClientFactory()));
    proxyingService.start();
    proxyTargetUrl = "http://localhost:" + wireMockServer.port();
    proxyingService.stubFor(
        proxyAllTo(proxyTargetUrl).withMetadata(metadata().attr("proxy", true)));

    client = new OkHttpClient();
    wireMockServer.stubFor(any(anyUrl()).willReturn(ok()));
  }

  @BeforeEach
  public void clearTargetServerMappings() {
    wireMockServer.resetMappings();
  }

  @AfterEach
  public void proxyServerShutdown() {
    // delete any persisted stub mappings to ensure test isolation
    proxyingService.resetMappings();
    proxyingService.stop();
  }


  @Test
  void canRecordAUrlThatDoesNotConformToRfc3986() throws Exception {
    proxyServerStart(wireMockConfig().withRootDirectory(setupTempFileRoot().getAbsolutePath()));
    Response response = client.newCall(new Builder().url(proxyingService.baseUrl() + "/foo/bar?q={}").build()).execute();
    System.err.println(response.body().string());
//    assertThat(response.code()).isEqualTo(200);
    List<ServeEvent> proxyTargetServeEvents = wireMockServer.getAllServeEvents();
    assertThat(proxyTargetServeEvents).hasSize(1);
    LoggedRequest targetRequest = proxyTargetServeEvents.get(0).getRequest();
    assertThat(targetRequest.getUrl()).isEqualTo("/foo/bar?q={}");
    assertThat(targetRequest.getQueryParams()).isEqualTo(Map.of("q", QueryParameter.queryParam("q", "{}")));
  }
}
