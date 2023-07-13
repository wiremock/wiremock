/*
 * Copyright (C) 2018-2021 Thomas Akehurst
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
package ignored;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.IOException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class VeryLongAsynchronousDelayAcceptanceTest {

  @RegisterExtension
  public WireMockExtension wireMockRule =
      WireMockExtension.newInstance().options(getOptions()).build();

  private WireMockConfiguration getOptions() {
    WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
    wireMockConfiguration.jettyAcceptors(1).containerThreads(4);
    wireMockConfiguration.asynchronousResponseEnabled(true);
    wireMockConfiguration.asynchronousResponseThreads(10);
    wireMockConfiguration.dynamicPort();
    return wireMockConfiguration;
  }

  @Test
  public void longDelayWithBodyMatch() throws IOException, ParseException {
    String json =
        "{ \"id\": \"cb7872bd-89cd-4015-97d1-718779df7dfe\", \"priority\": 1, \"request\": { \"urlPattern\": \"/faulty.*/.*/path/path\", \"method\": \"POST\", \"bodyPatterns\": [ { \"matches\": \".*<xml>permissions</xml>.*\" } ] }, \"response\": { \"status\": 200, \"bodyFileName\": \"plain-example.txt\", \"fixedDelayMilliseconds\": 35000 } }\n";

    wireMockRule.addStubMapping(Json.read(json, StubMapping.class));

    CloseableHttpResponse response =
        HttpClientFactory.createClient(50, 120000)
            .execute(
                ClassicRequestBuilder.post(wireMockRule.url("/faulty/1/path/path"))
                    .setEntity(new StringEntity("<xml>permissions</xml>"))
                    .build());

    assertThat(response.getCode(), is(200));
    assertThat(EntityUtils.toString(response.getEntity()), is("Some example test from a file"));
  }
}
