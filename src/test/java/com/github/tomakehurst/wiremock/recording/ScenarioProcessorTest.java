/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ScenarioProcessorTest {

  private final ScenarioProcessor processor = new ScenarioProcessor();

  @Test
  public void placesStubMappingsIntoScenariosWhenRepetitionsArePresent() {
    StubMapping foobar1 = WireMock.get("/foo/bar").willReturn(ok("fb-1")).build();
    StubMapping other1 = WireMock.get("/other").willReturn(ok("o-1")).build();
    StubMapping foobar2 = WireMock.get("/foo/bar").willReturn(ok("fb-2")).build();
    StubMapping foobar3 = WireMock.get("/foo/bar").willReturn(ok("fb-3")).build();
    StubMapping other2 = WireMock.get("/other").willReturn(ok("o-2")).build();

    List<StubMapping> stubs = asList(foobar1, other1, foobar2, foobar3, other2);
    Collections.reverse(stubs);
    List<StubMapping> processed = processor.putRepeatedRequestsInScenarios(stubs);

    final StubMapping processedFoobar1 = findStub(processed, foobar1);
    assertThat(processedFoobar1.getScenarioName(), is("scenario-2-foo-bar"));
    assertThat(processedFoobar1.getRequiredScenarioState(), is(Scenario.STARTED));
    assertThat(processedFoobar1.getNewScenarioState(), is("scenario-2-foo-bar-2"));

    final StubMapping processedFoobar2 = findStub(processed, foobar2);
    assertThat(processedFoobar2.getScenarioName(), is(processedFoobar1.getScenarioName()));
    assertThat(processedFoobar2.getRequiredScenarioState(), is("scenario-2-foo-bar-2"));
    assertThat(processedFoobar2.getNewScenarioState(), is("scenario-2-foo-bar-3"));

    final StubMapping processedFoobar3 = findStub(processed, foobar3);
    assertThat(processedFoobar1.getScenarioName(), is(processedFoobar3.getScenarioName()));
    assertThat(processedFoobar3.getRequiredScenarioState(), is("scenario-2-foo-bar-3"));
    assertThat(
        "Last mapping should not have a state transition",
        processedFoobar3.getNewScenarioState(),
        nullValue());

    final StubMapping processedOther1 = findStub(processed, other1);
    final StubMapping processedOther2 = findStub(processed, other2);
    assertThat(processedOther1.getScenarioName(), is("scenario-1-other"));
    assertThat(processedOther1.getNewScenarioState(), is("scenario-1-other-2"));
    assertThat(processedOther2.getRequiredScenarioState(), is("scenario-1-other-2"));
  }

  @Test
  public void doesNothingWhenNoRepeatedRequests() {
    StubMapping one = WireMock.get("/one").build();
    StubMapping two = WireMock.get("/two").build();
    StubMapping three = WireMock.get("/three").build();

    final List<StubMapping> processed = processor.putRepeatedRequestsInScenarios(asList(one, two, three));

    final StubMapping processedOne = findStub(processed, one);
    final StubMapping processedTwo = findStub(processed, two);
    final StubMapping processedThree = findStub(processed, three);
    assertThat(processedOne.getScenarioName(), nullValue());
    assertThat(processedTwo.getScenarioName(), nullValue());
    assertThat(processedThree.getScenarioName(), nullValue());
  }

  private static StubMapping findStub(List<StubMapping> processed, StubMapping foobar1) {
    return processed.stream().filter(stub -> stub.getId().equals(foobar1.getId())).findFirst().orElseThrow();
  }
}
