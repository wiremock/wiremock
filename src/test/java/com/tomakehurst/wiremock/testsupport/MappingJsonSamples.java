/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public static final String BASIC_ANY_METHOD = BASIC_GET.replace("GET", "ANY");
	
	public static final String MAPPING_REQUEST_WITH_EXACT_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/dependent\",				\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"equalTo\": \"text/xml\"			\n" +
		"			},										\n" +
		"			\"If-None-Match\": {					\n" +
		"				\"equalTo\": \"abcd1234\"			\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	public static final String MAPPING_REQUEST_WITH_REGEX_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/match/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"matches\": \"(.*)xml(.*)\"		\n" +
		"			},										\n" +
		"			\"If-None-Match\": {					\n" +
		"				\"matches\": \"([a-z0-9]*)\"		\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	public static final String MAPPING_REQUEST_WITH_NEGATIVE_REGEX_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/match/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"doesNotMatch\": \"(.*)xml(.*)\"	\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	

	public static final String WITH_REQUEST_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"PUT\",						\n" +
		"		\"url\": \"/header/matches/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": {						\n" +
		"				\"equalTo\": \"text/xml\"			\n" +
		"			},										\n" +
		"			\"If-None-Match\": {					\n" +
		"				\"matches\": \"([a-z0-9]*)\"		\n" +
		"			},										\n" +
		"			\"Accept\": {							\n" +
		"				\"doesNotMatch\": \"(.*)xml(.*)\"	\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 201								\n" +
		"	}												\n" +
		"}													";
}
