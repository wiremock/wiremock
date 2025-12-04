package com.github.tomakehurst.wiremock.http.client.okhttp;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import java.util.List;

public class OkHttpClientFactory implements HttpClientFactory {

  @Override
  public String getName() {
    return "okhttp-client-factory";
  }

  @Override
  public OkHttpClient buildHttpClient(Options options, boolean trustAllCertificates,
      List<String> trustedHosts, boolean useSystemProperties) {
    return new OkHttpClient();
  }
}
