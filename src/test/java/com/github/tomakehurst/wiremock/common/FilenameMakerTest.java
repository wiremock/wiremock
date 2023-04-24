/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilenameMakerTest {

  private FilenameMaker filenameMaker;

  @BeforeEach
  public void init() {
    filenameMaker = new FilenameMaker();
  }

  @Test
  public void generatesNameFromStubNameWhenPresent() {
    StubMapping mapping =
        WireMock.get("/named").withName("This is a NAMED stub").willReturn(ok()).build();

    assertThat(
        filenameMaker.filenameFor(mapping),
        is("this-is-a-named-stub-" + mapping.getId() + ".json"));
  }

  @Test
  public void generatesNameFromStubUrlWhenNameNotPresent() {
    FilenameMaker makerWithOwnFormat = new FilenameMaker("{{{method}}}-{{{url}}}.json");
    StubMapping mapping = WireMock.get(urlEqualTo("/named/123/things")).willReturn(ok()).build();

    assertThat(makerWithOwnFormat.filenameFor(mapping), is("get-named123things.json"));
  }

  @Test
  public void generatesNameFromStubUrlPathWhenNameNotPresent() {
    FilenameMaker makerWithOwnFormat = new FilenameMaker("{{{method}}}-{{{url}}}.json");
    StubMapping mapping =
        WireMock.get(urlPathEqualTo("/named/123/things")).willReturn(ok()).build();

    assertThat(makerWithOwnFormat.filenameFor(mapping), is("get-named123things.json"));
  }

  @Test
  public void generatesNameFromStubUrlPathTemplateWhenNameNotPresent() {
    FilenameMaker makerWithOwnFormat = new FilenameMaker("{{{method}}}-{{{url}}}.json");
    StubMapping mapping =
        WireMock.get(urlPathTemplate("/named/{id}/things")).willReturn(ok()).build();

    assertThat(makerWithOwnFormat.filenameFor(mapping), is("get-namedidthings.json"));
  }

  @Test
  public void generatesNameFromStubUrlPatternWhenNameNotPresent() {
    FilenameMaker makerWithOwnFormat = new FilenameMaker("{{{method}}}-{{{url}}}.json");
    StubMapping mapping =
        WireMock.get(urlMatching("/named/([0-9]*)/things")).willReturn(ok()).build();

    assertThat(makerWithOwnFormat.filenameFor(mapping), is("get-named0-9things.json"));
  }

  @Test
  public void generatesNameWhenStubUrlIsAnyAndNameNotPresent() {
    StubMapping mapping = WireMock.get(anyUrl()).willReturn(ok()).build();

    FilenameMaker makerWithOwnFormat = new FilenameMaker("{{{id}}}.json");

    assertThat(makerWithOwnFormat.filenameFor(mapping), is(mapping.getId() + ".json"));
  }

  @Test
  public void sanitizesUrlWithCharactersSafeForFilenames() {
    String output = filenameMaker.sanitizeUrl("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/and/¿?");
    assertThat(output, is("hello_1_2_3___ace--ace___and"));
  }

  @Test
  void generatesSanitizedFilename() {
    String filename =
        filenameMaker.filenameFor(
            get("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/and/¿?").willReturn(ok()).build());
    assertThat(filename, startsWith("get-hello123__--aceand-"));
  }

  @Test
  public void truncatesWhenResultingNameOver200Chars() {
    String output =
        filenameMaker.sanitizeUrl(
            "/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/andverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuff/¿?");
    assertThat(output.length(), is(200));
  }

  @Test
  void includesStubNameWhenPresent() {
    StubMapping stub =
        Json.read(
            "{\n"
                + "    \"name\": \"This is a NAMED stub\",\n"
                + "    \"persistent\": true,\n"
                + "    \"request\": {\n"
                + "        \"urlPath\": \"/one/two/three\",\n"
                + "        \"method\": \"GET\"\n"
                + "    },\n"
                + "\n"
                + "    \"response\": {\n"
                + "        \"status\": 200\n"
                + "    }\n"
                + "}\n",
            StubMapping.class);
    String filename = filenameMaker.filenameFor(stub);

    assertThat(filename, is("this-is-a-named-stub-" + stub.getId() + ".json"));
  }

  @Test
  void handlesAnAllDefaultsStub() {
    StubMapping stubMapping = Json.read("{}", StubMapping.class);

    String filename = filenameMaker.filenameFor(stubMapping);

    assertThat(filename, startsWith("any-always-"));
  }
}
