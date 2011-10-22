package com.tomakehurst.wiremock.testsupport;

public class MappingJsonSamples {

	public static final String BASIC_MAPPING_REQUEST =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/a/registered/resource\"	\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 401,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/plain\"		\n" +
		"		},											\n" +
		"		\"body\": \"Not allowed!\"					\n" +
		"	}												\n" +
		"}													";
	
	public static final String MAPPING_REQUEST_WITH_EXACT_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": \"text/xml\",				\n" +
		"			\"If-None-Match\": \"abcd1234\"			\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	public static final String STATUS_ONLY_MAPPING_REQUEST =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"PUT\",						\n" +
		"		\"url\": \"/status/only\"			\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 204								\n" +
		"	}												\n" +
		"}													";
	
	public static final String STATUS_ONLY_GET_MAPPING_TEMPLATE =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"urlPattern\": \"%s\"						\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200								\n" +
		"	}												\n" +
		"}													";
	
}
