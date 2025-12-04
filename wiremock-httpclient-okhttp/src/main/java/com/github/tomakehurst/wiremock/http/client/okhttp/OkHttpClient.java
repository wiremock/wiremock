package com.github.tomakehurst.wiremock.http.client.okhttp;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import java.io.IOException;

public class OkHttpClient implements HttpClient {

  @Override
  public Response execute(Request request) throws IOException {
    throw new UnsupportedOperationException();
  }
}
