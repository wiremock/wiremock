package com.github.tomakehurst.wiremock.http;

public interface HTTPBasicMessage {
	HttpHeaders getHeaders();
	byte[] getBody();
}