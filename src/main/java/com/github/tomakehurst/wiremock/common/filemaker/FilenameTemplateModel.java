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
package com.github.tomakehurst.wiremock.common.filemaker;

import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.UUID;

public class FilenameTemplateModel {

  private final StubMapping stubMapping;

  public FilenameTemplateModel(StubMapping stubMapping) {
    this.stubMapping = stubMapping;
  }

  public UUID getId() {
    return stubMapping.getId();
  }

  public String getName() {
    return stubMapping.getName();
  }

  public String getUrl() {
    return stubMapping.getRequest().getUrlMatcher().getExpected();
  }

  public String getMethod() {
    return stubMapping.getRequest().getMethod().getName();
  }

  public Integer getPriority() {
    return stubMapping.getPriority();
  }

  public String getScenarioName() {
    return stubMapping.getScenarioName();
  }

  public String getRequiredScenarioState() {
    return stubMapping.getRequiredScenarioState();
  }

  public String getNewScenarioState() {
    return stubMapping.getNewScenarioState();
  }

  public RequestPattern getRequest() {
    return stubMapping.getRequest();
  }

  public ResponseDefinition getResponse() {
    return stubMapping.getResponse();
  }

  public Metadata getMetadata() {
    return stubMapping.getMetadata();
  }

  public long getInsertionIndex() {
    return stubMapping.getInsertionIndex();
  }
}
