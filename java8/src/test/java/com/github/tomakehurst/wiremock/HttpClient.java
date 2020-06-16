package com.github.tomakehurst.wiremock;

interface HttpClient {
    Response get(String url) throws Exception;
}
