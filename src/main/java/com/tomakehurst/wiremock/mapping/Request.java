package com.tomakehurst.wiremock.mapping;

import java.util.Set;

import com.tomakehurst.wiremock.http.RequestMethod;

public interface Request {

	String getUrl();
	RequestMethod getMethod();
	String getHeader(String key);
	boolean containsHeader(String key);
	Set<String> getAllHeaderKeys();
	String getBodyAsString();

}