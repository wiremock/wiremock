/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

/** The type Filename template model. */
public class FilenameTemplateModel {

  private final StubMapping stubMapping;

  /**
   * Instantiates a new Filename template model.
   *
   * @param stubMapping the stub mapping
   */
  public FilenameTemplateModel(StubMapping stubMapping) {
    this.stubMapping = stubMapping;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public UUID getId() {
    return stubMapping.getId();
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return stubMapping.getName();
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return stubMapping.getRequest().getUrlMatcher().getExpected();
  }

  /**
   * Gets method.
   *
   * @return the method
   */
  public String getMethod() {
    return stubMapping.getRequest().getMethod().getName();
  }

  /**
   * Gets priority.
   *
   * @return the priority
   */
  public Integer getPriority() {
    return stubMapping.getPriority();
  }

  /**
   * Gets scenario name.
   *
   * @return the scenario name
   */
  public String getScenarioName() {
    return stubMapping.getScenarioName();
  }

  /**
   * Gets request.
   *
   * @return the request
   */
  public RequestPattern getRequest() {
    return stubMapping.getRequest();
  }

  /**
   * Gets response.
   *
   * @return the response
   */
  public ResponseDefinition getResponse() {
    return stubMapping.getResponse();
  }

  /**
   * Gets metadata.
   *
   * @return the metadata
   */
  public Metadata getMetadata() {
    return stubMapping.getMetadata();
  }
}
