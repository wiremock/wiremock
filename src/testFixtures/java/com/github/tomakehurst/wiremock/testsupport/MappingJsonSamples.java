/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;

public class MappingJsonSamples {

  public static final String BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER =
      """
          {
          	"request": {
          		"method": "GET",
          		"url": "/a/registered/resource"
          	},
          	"response": {
          		"status": 401,
          		"headers": {
          			"Content-Type": "text/plain"
          		},
          		"body": "Not allowed!"
          	}
          }""";

  public static final String STATUS_ONLY_MAPPING_REQUEST =
      """
          {
          	"request": {
          		"method": "PUT",
          		"url": "/status/only"
          	},
          	"response": {
          		"status": 204
          	}
          }""";

  public static final String STATUS_ONLY_GET_MAPPING_TEMPLATE =
      """
          {
          	"request": {
          		"method": "GET",
          		"urlPattern": "%s"
          	},
          	"response": {
          		"status": 200
          	}
          }""";

  public static final String BASIC_GET =
      """
          {
          	"name": "Basic Resource",
          	"request": {
          		"method": "GET",
          		"url": "/basic/mapping/resource"
          	},
          	"response": {
          		"status": 304,
          		"body": "Body from mapping file"
          	}
          }""";

  public static final String BASIC_POST = BASIC_GET.replace("GET", "POST");
  public static final String BASIC_PUT = BASIC_GET.replace("GET", "PUT");
  public static final String BASIC_DELETE = BASIC_GET.replace("GET", "DELETE");
  public static final String BASIC_PATCH = BASIC_GET.replace("GET", "PATCH");
  public static final String BASIC_HEAD = BASIC_GET.replace("GET", "HEAD");
  public static final String BASIC_OPTIONS = BASIC_GET.replace("GET", "OPTIONS");
  public static final String BASIC_TRACE = BASIC_GET.replace("GET", "TRACE");
  public static final String BASIC_ANY_METHOD = BASIC_GET.replace("GET", "ANY");
  public static final String BASIC_QUERY = BASIC_GET.replace("GET", "QUERY");

  public static final String MAPPING_REQUEST_WITH_EXACT_HEADERS =
      """
          {
          	"request": {
          		"method": "GET",
          		"url": "/header/dependent",
          		"headers": {
          			"Accept": {
          				"equalTo": "text/xml"
          			},
          			"If-None-Match": {
          				"equalTo": "abcd1234"
          			}
          		}
          	},
          	"response": {
          		"status": 304,
          		"headers": {
          			"Content-Type": "text/xml"
          		}
          	}
          }""";

  public static final String MAPPING_REQUEST_WITH_REGEX_HEADERS =
      """
          {
          	"request": {
          		"method": "GET",
          		"url": "/header/match/dependent",
          		"headers": {
          			"Accept": {
          				"matches": "(.*)xml(.*)"
          			},
          			"If-None-Match": {
          				"matches": "([a-z0-9]*)"
          			}
          		}
          	},
          	"response": {
          		"status": 304,
          		"headers": {
          			"Content-Type": "text/xml"
          		}
          	}
          }""";

  public static final String MAPPING_REQUEST_WITH_NEGATIVE_REGEX_HEADERS =
      """
          {
          	"request": {
          		"method": "GET",
          		"url": "/header/match/dependent",
          		"headers": {
          			"Accept": {
          				"doesNotMatch": "(.*)xml(.*)"
          			}
          		}
          	},
          	"response": {
          		"status": 200,
          		"headers": {
          			"Content-Type": "text/xml"
          		}
          	}
          }""";

  public static final byte[] BINARY_COMPRESSED_CONTENT =
      new byte[] {
        31, -117, 8, 8, 72, -53, -8, 79, 0, 3, 103, 122, 105, 112, 100, 97, 116, 97, 45, 111, 117,
        116, 0, -77, 41, 74, 45, 46, -56, -49, 43, 78, -75, -53, 72, -51, -55, -55, -73, -47, -121,
        -13, 1, 9, 69, -3, 52, 26, 0, 0, 0
      };
  public static final String BINARY_COMPRESSED_JSON_STRING =
      encodeBase64(BINARY_COMPRESSED_CONTENT);
  public static final String BINARY_COMPRESSED_CONTENT_AS_STRING = "<response>hello</response>";

  public static final String MAPPING_REQUEST_FOR_BYTE_BODY =
      """
        {
          "request": {
            "method": "GET",
            "url": "/byte/resource/from/file"
          },
          "response": {
            "status": 200,
            "base64Body": "%s"
          }
        }"""
          .formatted(encodeBase64(new byte[] {65, 66, 67}));

  public static final String MAPPING_REQUEST_FOR_BINARY_BYTE_BODY =
      """
        {
          "request": {
            "method": "GET",
            "url": "/bytecompressed/resource/from/file"
          },
          "response": {
            "status": 200,
            "base64Body": "%s"
          }
        }"""
          .formatted(BINARY_COMPRESSED_JSON_STRING);

  public static final String MAPPING_REQUEST_FOR_NON_UTF8 =
      """
          {
             "request": {
                     "method": "GET",
                     "url": "/test/nonutf8/"
             },
             "response": {
                     "status": 200,
                     "headers": {
                         "Content-type": "text/plain; charset=GB2312"
                     },
                     "body": "国家标准"
             }
          }
          """;

  public static final String MAPPING_REQUEST_JSON_BODY_DECIMALS_NO_TRAILING_ZEROS =
      """
          {
          	"request": {
          		"method": "POST",
          		"url": "/body/decimals",
          		"bodyPatterns": [
          			{ "equalToJson": {"float": 1.2} }
          		]
          	},
          	"response": {
          		"status": 200
          	}
          }""";

  public static final String MAPPING_REQUEST_JSON_BODY_DECIMALS_TRAILING_ZEROS =
      MAPPING_REQUEST_JSON_BODY_DECIMALS_NO_TRAILING_ZEROS.replace("1.2", "1.20000000");
}
