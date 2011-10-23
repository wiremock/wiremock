package com.tomakehurst.wiremock.testsupport;

public class MappingJsonSamples {

	public static final String BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/a/registered/resource\"			\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 401,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/plain\"		\n" +
		"		},											\n" +
		"		\"body\": \"Not allowed!\"					\n" +
		"	}												\n" +
		"}													";
	
	public static final String STATUS_ONLY_MAPPING_REQUEST =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"PUT\",						\n" +
		"		\"url\": \"/status/only\"					\n" +
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
	
	public static final String WITH_RESPONSE_BODY =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/with/body\"						\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200,							\n" +
		"		\"body\": \"Some content\"					\n" +
		"	}												\n" +
		"}													";
	
	public static final String BASIC_GET =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/basic/mapping/resource\"		\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"	}												\n" +
		"}													";
	
	public static final String BASIC_POST = BASIC_GET.replace("GET", "POST");
	public static final String BASIC_PUT = BASIC_GET.replace("GET", "PUT");
	public static final String BASIC_DELETE = BASIC_GET.replace("GET", "DELETE");
	public static final String BASIC_HEAD = BASIC_GET.replace("GET", "HEAD");
	public static final String BASIC_OPTIONS = BASIC_GET.replace("GET", "OPTIONS");
	public static final String BASIC_TRACE = BASIC_GET.replace("GET", "TRACE");
}
