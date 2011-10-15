package com.tomakehurst.wiremock.testsupport;

public class MappingJsonSamples {

	public static final String BASIC_MAPPING_REQUEST_JSON =
		"{ 													" +
		"	\"request\": {									" +
		"		\"method\": \"GET\",						" +
		"		\"uriPattern\": \"/a/registered/resource\"	" +
		"	},												" +
		"	\"response\": {									" +
		"		\"status\": 401,							" +
		"		\"headers\": {								" +
		"			\"Content-Type\": \"text/plain\"		" +
		"		},											" +
		"		\"body\": \"Not allowed!\"					" +
		"	}												" +
		"}													";
}
