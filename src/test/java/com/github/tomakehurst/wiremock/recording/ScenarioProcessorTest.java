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

@SuppressWarnings("removal")
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
    processor.putRepeatedRequestsInScenarios(stubs);

    assertThat(foobar1.getScenarioName(), is("scenario-2-foo-bar"));
    assertThat(foobar1.getRequiredScenarioState(), is(Scenario.STARTED));
    assertThat(foobar1.getNewScenarioState(), is("scenario-2-foo-bar-2"));

    assertThat(foobar2.getScenarioName(), is(foobar1.getScenarioName()));
    assertThat(foobar2.getRequiredScenarioState(), is("scenario-2-foo-bar-2"));
    assertThat(foobar2.getNewScenarioState(), is("scenario-2-foo-bar-3"));

    assertThat(foobar1.getScenarioName(), is(foobar3.getScenarioName()));
    assertThat(foobar3.getRequiredScenarioState(), is("scenario-2-foo-bar-3"));
    assertThat(
        "Last mapping should not have a state transition",
        foobar3.getNewScenarioState(),
        nullValue());

    assertThat(other1.getScenarioName(), is("scenario-1-other"));
    assertThat(other1.getNewScenarioState(), is("scenario-1-other-2"));
    assertThat(other2.getRequiredScenarioState(), is("scenario-1-other-2"));
  }

  @Test
  public void doesNothingWhenNoRepeatedRequests() {
    StubMapping one = WireMock.get("/one").build();
    StubMapping two = WireMock.get("/two").build();
    StubMapping three = WireMock.get("/three").build();

    processor.putRepeatedRequestsInScenarios(asList(one, two, three));

    assertThat(one.getScenarioName(), nullValue());
    assertThat(two.getScenarioName(), nullValue());
    assertThat(three.getScenarioName(), nullValue());
  }
}
