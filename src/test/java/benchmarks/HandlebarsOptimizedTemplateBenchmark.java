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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.stubbing.ServeEventFactory.newPostMatchServeEvent;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.ExtensionFactoryUtils;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2)
@Fork(1)
@Measurement(iterations = 5)
public class HandlebarsOptimizedTemplateBenchmark {

  @State(Scope.Benchmark)
  public static class HandlebarsOptimizedTemplateBenchmarkState {
    private ResponseTemplateTransformer transformer;

    @Setup
    public void setup() {
      transformer = ExtensionFactoryUtils.buildTemplateTransformer(true);
    }
  }

  @Benchmark
  @Threads(50)
  public boolean transform(HandlebarsOptimizedTemplateBenchmarkState state) {

    String result =
        transform(
            "{{#each (range 100000 199999) as |index|}}Line {{index}}\n{{/each}}",
            state.transformer);

    boolean hasCorrectStart = result.startsWith("Line 100000\nLine 100001\nLine 100002\n");
    boolean hasCorrectLength = result.length() == 1_200_000;
    return hasCorrectStart && hasCorrectLength;
  }

  private String transform(String responseBodyTemplate, ResponseTemplateTransformer transformer) {
    final ResponseDefinitionBuilder responseDefinitionBuilder =
        aResponse().withBody(responseBodyTemplate);
    final StubMapping stub = get("/").willReturn(responseDefinitionBuilder).build();
    final MockRequest request = mockRequest();
    ServeEvent serveEvent = newPostMatchServeEvent(request, responseDefinitionBuilder, stub);
    return transformer.transform(serveEvent).getBody();
  }
}
