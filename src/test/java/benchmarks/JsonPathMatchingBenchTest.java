/*
 * Copyright (C) 2024 Thomas Akehurst
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
package benchmarks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JsonPathMatchingBenchTest {

  public static final String[] TOPICS = {
    "topic-one", "longer-topic-2", "very-long-topic-3", "four", "five55555555"
  };

  private WireMockServer wm;
  private WireMockTestClient client;

  @BeforeEach
  public void setup() {
    wm = new WireMockServer(wireMockConfig().dynamicPort());
    wm.start();
    client = new WireMockTestClient(wm.port());

    for (String topic : TOPICS) {
      wm.stubFor(
          post("/things").withRequestBody(matchingJsonPath("$.[?(@.topic == '" + topic + "')]")).willReturn(ok(topic)));
    }
  }

  @Test
  public void matched() {
    final WireMockResponse response =
        client.postJson("/things", String.format(JSON_TEMPLATE, "topic-one"));
    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("topic-one"));
  }

  public static void main(String[] args) throws Exception {
    Options opt =
        new OptionsBuilder()
            .include(JsonPathMatchingBenchTest.class.getSimpleName())
            .warmupIterations(3)
            .forks(5)
            .measurementIterations(10)
            .build();

    new Runner(opt).run();
  }

  static final String JSON_TEMPLATE =
      "[\n"
          + "    {\n"
          + "        \"topic\": \"%s\",\n"
          + "        \"key\": {\n"
          + "            \"orgId\": \"ORG001\",\n"
          + "            \"productId\": \"1266009\",\n"
          + "            \"uom\": \"EACH\",\n"
          + "            \"locationType\": \"STORE\",\n"
          + "            \"locationId\": \"S001\",\n"
          + "            \"sellingChannel\": \"dotcom\",\n"
          + "            \"fulfillmentType\": \"SHIP\"\n"
          + "        },\n"
          + "        \"value\": {\n"
          + "            \"orgId\": \"ORG001\",\n"
          + "            \"productId\": \"1266009\",\n"
          + "            \"uom\": \"EACH\",\n"
          + "            \"locationType\": \"STORE\",\n"
          + "            \"locationId\": \"S001\",\n"
          + "            \"sellingChannel\": \"dotcom\",\n"
          + "            \"fulfillmentType\": \"SHIP\",\n"
          + "            \"enabled\": true,\n"
          + "            \"systemProcessingTime\": {\n"
          + "                \"min\": 2,\n"
          + "                \"max\": 5\n"
          + "            },\n"
          + "            \"transactionalSystemProcessingTime\": {\n"
          + "                \"additionalProp1\": {\n"
          + "                    \"min\": 2,\n"
          + "                    \"max\": 5\n"
          + "                },\n"
          + "                \"additionalProp2\": {\n"
          + "                    \"min\": 2,\n"
          + "                    \"max\": 5\n"
          + "                },\n"
          + "                \"additionalProp3\": {\n"
          + "                    \"min\": 2,\n"
          + "                    \"max\": 5\n"
          + "                }\n"
          + "            },\n"
          + "            \"locationProcessingTime\": {\n"
          + "                \"min\": 2,\n"
          + "                \"max\": 5\n"
          + "            },\n"
          + "            \"safetyStock\": 2,\n"
          + "            \"shelfLife\": 5,\n"
          + "            \"temporaryDisableExpirationTime\": \"2018-06-26T23:24:08.255Z\",\n"
          + "            \"updateUser\": \"THOR\",\n"
          + "            \"updateTime\": \"2018-06-21T00:00:00Z\"\n"
          + "        },\n"
          + "        \"operation\": \"CREATE\",\n"
          + "        \"isFullyQualifiedTopicName\": false\n"
          + "    }\n"
          + "]";
}
