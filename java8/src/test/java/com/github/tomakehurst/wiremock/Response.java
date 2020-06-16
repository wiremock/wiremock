package com.github.tomakehurst.wiremock;

class Response {
    final int status;
    final String body;

    Response(int status, String body) {
        this.status = status;
        this.body = body;
    }
}
